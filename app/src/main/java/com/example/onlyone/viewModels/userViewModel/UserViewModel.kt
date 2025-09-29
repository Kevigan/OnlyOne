package com.example.onlyone.viewModels.userViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dao.FriendDao
import com.example.onlyone.BuildConfig
import com.example.onlyone.ads.AdCounter
import com.example.onlyone.cloudMessaging.MessageNotifier
import com.example.onlyone.data.LocalFavoriteMessage
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.data.achievementDefinitions.MessageAchievements
import com.example.onlyone.repos.userRepos.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # UserViewModel
 *
 * Single source of truth for:
 * - The signed-in user's composite profile (`UserComposite`)
 * - Engagement state (swipe counters, daily limits, etc.)
 * - High-level feature operations via manager facades (user, swipe, shop, settings, upgrades, achievements)
 * - Ad pacing and minor UI-side gating
 *
 * Notes:
 * - Firebase Auth is observed to (re)hydrate user data on account changes.
 * - Reads are delegated to repositories/managers to keep the VM lean.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val friendDao: FriendDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    // Expose the repository for existing callers like SetUsernameView
    val repository: UserRepository
        get() = userRepository

    // ---------------------------
    // User (profile) state
    // ---------------------------

    private val _user = MutableLiveData<UserComposite?>()
    /** Public user composite (null when signed out). */
    val user: LiveData<UserComposite?> get() = _user

    // User requests & block lists proxied from UserManager (StateFlows for easy compose collection)
    val incomingRequestUsernames: StateFlow<Map<String, String>> get() = userManager.incomingRequestUsernames
    val outgoingRequestUsernames: StateFlow<Map<String, String>> get() = userManager.outgoingRequestUsernames
    val blockedUsers: StateFlow<List<PublicUser>> get() = userManager.blockedUsers

    // ---------------------------
    // Engagement state (limits, counters)
    // ---------------------------

    private val _engagementStatus = MutableStateFlow<UserEngagementStatus?>(null)
    val engagementStatus: StateFlow<UserEngagementStatus?> = _engagementStatus.asStateFlow()

    // ---------------------------
    // Swipe feed (random user queue) facades for UI
    // ---------------------------

    val userQueue get() = swipeManager.userQueue
    val targetUser get() = swipeManager.targetUser
    val isLoadingUserBatch get() = swipeManager.isLoadingUserBatch
    val lastUserLoadResult get() = swipeManager.lastUserLoadResult
    val toastEvent get() = swipeManager.toastEvent

    // Avoid reloading the same message thread repeatedly
    private var lastLoadedMessageUid: String? = null

    // Track which Firebase UID we hydrated last (prevents duplicate work on resume/rotation)
    private var lastUidLoaded: String? = null

    // ---------------------------
    // Firebase Auth wiring
    // ---------------------------

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { fa -> onAuthUserChanged(fa.currentUser) }

    // ---------------------------
    // Ads pacing
    // ---------------------------

    private val adCounter = AdCounter(appContext)
    val adCountToday: Flow<Int> = adCounter.countToday
    /** True on every 10th random card view (for showing an interstitial, etc.). */
    val showAdFlow: Flow<Boolean> = adCountToday.map { it > 0 && it % 10 == 0 }

    /** Call when a random card becomes visible to bump ad pacing. */
    fun onRandomCardVisible() { viewModelScope.launch { adCounter.increment() } }

    // ---------------------------
    // Managers (feature facades)
    // ---------------------------

    // user
    var userManager: UserManager
    // upgrades
    var upgradeManager: UpgradeManager
    // settings
    var settingsManager: UserSettingsManager
    // swipe
    var swipeManager: SwipeManager
    // shop
    var shopManager: ShopManager
    // achievements
    var achievementManager: AchievementManager

    // ---------------------------
    // Initialization
    // ---------------------------

    init {
        // Build managers
        userManager = UserManager(
            userRepository,
            viewModelScope,
            updateUser = { incoming ->
                // ✅ No recompute. Trust the server-mirrored values in users_public.
                _user.value = incoming
            },
            updateEngagementStatus = { _engagementStatus.value = it },
            getUser = { _user.value }
        )

        upgradeManager = UpgradeManager(
            userRepository = userRepository,
            getUser = { _user.value },
            updateUser = { _user.value = it }
        )

        settingsManager = UserSettingsManager(
            userRepository,
            viewModelScope,
            getUid = { _user.value?.uid }
        )

        swipeManager = SwipeManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value },
            getEngagement = { _engagementStatus.value },
            updateEngagement = { _engagementStatus.value = it },
            refreshEngagementStatus = { refreshEngagementStatus() }
        )

        shopManager = ShopManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value },
            updateUser = { _user.value = it }
        )

        achievementManager = AchievementManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value }
        )

        // Start listening to auth and prime immediately
        auth.addAuthStateListener(authListener)
        onAuthUserChanged(auth.currentUser) // in case the listener triggers late

        // Listen for push "feedback" events that require a quick inventory refresh
        viewModelScope.launch {
            MessageNotifier.newMessageFlow.collect { (title, _) ->
                if (title?.contains("feedback", ignoreCase = true) == true) {
                    fetchUserInventory()
                }
            }
        }
    }

    // ---------------------------
    // Auth change handling
    // ---------------------------

    /**
     * Central handler whenever Firebase auth user changes (including app resume).
     * Ensures we only hydrate once per UID until it changes.
     */
    private fun onAuthUserChanged(current: FirebaseUser?) {
        if (current == null) {
            lastUidLoaded = null
            _user.postValue(null)
            return
        }
        if (current.uid == lastUidLoaded && _user.value != null) return

        lastUidLoaded = current.uid
        loadUser()
        refreshEngagementStatus()
    }

    // ---------------------------
    // User APIs (forwarding to UserManager)
    // ---------------------------

    fun loadUser() = userManager.loadUser()

    /** Safely mutate the current user in-place (no network). */
    fun updateUserLocal(transform: (UserComposite) -> UserComposite) {
        val cur = _user.value ?: return
        _user.postValue(transform(cur))
    }

    /** Convenience: apply a reward delta after sending a message. */
    fun applySendRewards(goldDelta: Int, pointsDelta: Int, rune: String?) {
        updateUserLocal { cur ->
            cur.copy(
                gold = cur.gold + goldDelta,
                points = cur.points + pointsDelta,
                runes_rare = if (rune == "rare") cur.runes_rare + 1 else cur.runes_rare,
                runes_super_rare = if (rune == "super_rare") cur.runes_super_rare + 1 else cur.runes_super_rare,
                runes_mega_rare = if (rune == "mega_rare") cur.runes_mega_rare + 1 else cur.runes_mega_rare
            )
        }
    }

    fun updatePublicProfile(
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = userManager.updatePublicProfile(updates, onSuccess, onFailure)

    fun blockUser(targetUid: String) = userManager.blockUser(targetUid)
    fun unblockUser(targetUid: String) = userManager.unblockUser(targetUid)

    fun sendFriendRequestByEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) = userManager.sendFriendRequestByEmail(email, onSuccess, onFailure)

    fun sendFriendRequestDirect(targetUid: String) = userManager.sendFriendRequestDirect(targetUid)

    fun cancelOutgoingFriendRequest(
        targetUid: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) = userManager.cancelOutgoingFriendRequest(targetUid, onSuccess, onFailure)

    fun acceptFriendRequest(requesterUid: String) = userManager.acceptFriendRequest(requesterUid)
    fun declineFriendRequest(requesterUid: String) = userManager.declineFriendRequest(requesterUid)
    fun deleteFriend(friendUid: String) = userManager.deleteFriend(friendUid)

    fun fetchUserInventory() = userManager.fetchUserInventory()

    fun clearFavouriteMessage(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) = userManager.clearFavouriteMessage(onSuccess, onFailure)

    fun setFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) = userManager.setFavouriteMessage(fav, onSuccess, onFailure)

    fun toggleFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) = userManager.toggleFavouriteMessage(fav, onSuccess, onFailure)

    // ---------------------------
    // Upgrades
    // ---------------------------

    fun upgradeFeature(
        feature: String,
        levels: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = upgradeManager.upgradeFeature(feature, levels, onSuccess, onFailure)

    // ---------------------------
    // Settings
    // ---------------------------

    fun updateNotificationPreference(
        key: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) = settingsManager.updateNotificationPreference(key, enabled, onSuccess, onFailure)

    fun saveAppLanguage(language: String) = settingsManager.saveAppLanguage(language)
    fun getAppLanguage(onResult: (String) -> Unit) = settingsManager.getAppLanguage(onResult)

    fun saveNotificationToggles(message: Boolean, feedback: Boolean) =
        settingsManager.saveNotificationToggles(message, feedback)

    fun getNotificationToggles(onResult: (Boolean, Boolean) -> Unit) =
        settingsManager.getNotificationToggles(onResult)

    fun saveSearchUserLanguage(lang: String) = settingsManager.saveSearchUserLanguage(lang)
    fun getSearchUserLanguage(onResult: (String) -> Unit) = settingsManager.getSearchUserLanguage(onResult)

    // ---------------------------
    // Swipe / Discovery
    // ---------------------------

    fun loadRandomUserBatchIfNeeded(showToasts: Boolean = false) =
        swipeManager.loadRandomUserBatchIfNeeded(showToasts)

    suspend fun loadRandomUserBatch(showToasts: Boolean = false): SwipeManager.RandomUserLoadResult =
        swipeManager.loadRandomUserBatch(showToasts)

    fun consumeNextUserFromQueue() = swipeManager.consumeNextUserFromQueue()
    fun setTargetUser(user: PublicUser) = swipeManager.setTargetUser(user)

    fun resetSwipesAfterAd(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        swipeManager.resetSwipesWithAd { ok, msg ->
            if (ok) {
                refreshEngagementStatus() // stay in sync with server
                onSuccess()
            } else {
                onError(msg ?: "Reset failed")
            }
        }
    }

    // ---------------------------
    // Shop
    // ---------------------------

    fun buyAvatar(avatarId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        shopManager.buyAvatar(avatarId, onSuccess, onFailure)

    fun buyMood(moodId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        shopManager.buyMood(moodId, onSuccess, onFailure)

    fun buyTheme(themeId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) =
        shopManager.buyTheme(themeId, onSuccess, onFailure)

    // ---------------------------
    // Achievements
    // ---------------------------

    fun loadAchievementsWithStats() {
        // Definitions are local (MessageAchievements.definitions), only user data comes from repository
        achievementManager.loadUserAchievementsWithStats(
            onSuccess = { updateGroupedAchievements() },
            onFailure = { error ->
                Log.e("Achievements", "❌ Failed to load: $error")
            }
        )
    }

    private val _groupedAchievements =
        MutableLiveData<Map<String, List<AchievementManager.AchievementWithProgress>>>()
    val groupedAchievements: LiveData<Map<String, List<AchievementManager.AchievementWithProgress>>> =
        _groupedAchievements

    /** Group loaded achievements for the UI. */
    fun updateGroupedAchievements() {
        val defs = MessageAchievements.definitions(appContext)
        val user = achievementManager.getCachedUserAchievements() ?: run {
            Log.e("Achievements", "❌ No achievement user data cached")
            return
        }
        val achieved = user["achieved"] as? Map<String, Any> ?: emptyMap()

        Log.d("Achievements", "✅ Definitions count: ${defs.size}")
        Log.d("Achievements", "✅ Achieved keys: ${achieved.keys}")
        Log.d("Achievements", "✅ Cached stats: ${achievementManager.cachedUserStats}")

        val grouped = achievementManager.groupAchievementsByType(defs, achieved)
        Log.d("Achievements", "✅ Grouped count: ${grouped.size}")
        _groupedAchievements.value = grouped
    }

    // ---------------------------
    // Feedback
    // ---------------------------

    sealed class FeedbackUiState {
        object Idle : FeedbackUiState()
        object Sending : FeedbackUiState()
        object Success : FeedbackUiState()
        object AlreadySubmitted : FeedbackUiState()
        data class Error(val message: String) : FeedbackUiState()
    }

    private val _feedbackState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Idle)
    val feedbackState: StateFlow<FeedbackUiState> = _feedbackState.asStateFlow()

    fun clearFeedbackState() {
        _feedbackState.value = FeedbackUiState.Idle
    }

    fun sendFeedback(
        answers: Map<String, String>,
        text: String = "",
        platform: String = "android",
        appVersion: String = BuildConfig.VERSION_NAME,
        lang: String = "en"
    ) {
        _feedbackState.value = FeedbackUiState.Sending
        viewModelScope.launch {
            when (val res = userRepository.sendUserFeedback(
                answers = answers,
                text = text,
                platform = platform,
                appVersion = appVersion,
                lang = lang
            )) {
                is UserRepository.FeedbackResult.Success -> _feedbackState.value = FeedbackUiState.Success
                is UserRepository.FeedbackResult.AlreadySubmitted -> _feedbackState.value = FeedbackUiState.AlreadySubmitted
                is UserRepository.FeedbackResult.Error -> _feedbackState.value = FeedbackUiState.Error(res.message)
            }
        }
    }

    // ---------------------------
    // Engagement helpers
    // ---------------------------

    /** Return true if messages for [uid] should be (re)loaded (guards duplicate work). */
    fun shouldLoadMessagesFor(uid: String): Boolean {
        return if (uid != lastLoadedMessageUid) {
            lastLoadedMessageUid = uid
            true
        } else {
            false
        }
    }

    /** Refresh engagement limits/counters for the current user. */
    fun refreshEngagementStatus() {
        val uid = _user.value?.uid ?: return
        userRepository.fetchEngagementStatus(uid) { status ->
            if (status != null) {
                _engagementStatus.value = status
                Log.d("UserViewModel", "✅ EngagementStatus refreshed")
            }
        }
    }

    // ---------------------------
    // Local storage utilities
    // ---------------------------

    /** Live stream of locally cached friends (Room/DAO). */
    fun observeLocalFriends(): Flow<List<LocalFriend>> = friendDao.getAllFriends()

    /** Clear friends both locally & remotely as implemented in repository. */
    fun hardResetFriends() {
        viewModelScope.launch { userRepository.hardResetFriends() }
    }

    /** Clear local messages as implemented in repository. */
    fun hardResetLocalMessages() {
        viewModelScope.launch { userRepository.hardResetLocalMessages() }
    }

    // ---------------------------
    // Debug / tooling
    // ---------------------------

    /** Cloud function to create fake users for testing (europe-west3). */
    fun createFakeUsers(count: Int) {
        Firebase.functions("europe-west3")
            .getHttpsCallable("createFakeUsers")
            .call(mapOf("debug" to true, "count" to count))
            .addOnSuccessListener { result ->
                Log.d("FakeUsers", "✅ Created: ${(result.data as? Map<*, *>)?.get("created")}")
            }
            .addOnFailureListener { error ->
                Log.e("FakeUsers", "❌ Failed to create fake users", error)
            }
    }

    /** Seed achievement definitions from local definitions into backend. */
    fun seedAchievementDefinitions(
        onSuccess: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.seedAchievementDefinitions(
            definitions = MessageAchievements.definitions(appContext),
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    // ---------------------------
    // Lifecycle
    // ---------------------------

    override fun onCleared() {
        // Remove listener to avoid leaks
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }
}
