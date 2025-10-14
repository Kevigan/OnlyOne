package com.onlyone.app.viewModels.userViewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.onlyone.app.dao.FriendDao
import com.onlyone.app.ads.AdCounter
import com.onlyone.app.cloudMessaging.MessageNotifier
import com.onlyone.app.data.LocalFavoriteMessage
import com.onlyone.app.data.LocalFriend
import com.onlyone.app.data.PublicUser
import com.onlyone.app.data.UserComposite
import com.onlyone.app.data.UserEngagementStatus
import com.onlyone.app.data.achievementDefinitions.MessageAchievements
import com.onlyone.app.repos.userRepos.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.onlyone.app.BuildConfig
import com.onlyone.app.utils.normalizeLang
import com.onlyone.app.viewModels.chain.ChainManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
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
    // ---------------------------
// App language (source of truth for UI)
// ---------------------------
    private val _appLanguage = MutableStateFlow(
        com.onlyone.app.utils.currentAppLanguageCode() // instead of "de"
    )
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

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

    //ChainMessages
    lateinit var chainManager: ChainManager
        private set
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

        // Build managers
        chainManager = ChainManager(
            userRepository = userRepository,
            scope = viewModelScope
        )

        // If you have a flow of current user, start chainManager when UID is known.
        // For example:
        viewModelScope.launch {
            // ✅ observe LiveData as a Flow
            user.asFlow()
                .map { it?.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    if (uid != null) {
                        Log.d("UserViewModel", "▶️ chainManager.start($uid)")
                        chainManager.start(uid)
                    } else {
                        Log.d("UserViewModel", "⏹ chainManager.stop()")
                        chainManager.stop()
                    }
                }
        }

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

    /** Persist + apply (debounced/no-op if unchanged) */
    fun setAppLanguageAndApply(code: String) {
        val normalized = code.trim().lowercase(Locale.ROOT)
            .split('-', '_').firstOrNull().orEmpty()
        if (normalized.isEmpty() || normalized == _appLanguage.value) return

        _appLanguage.value = normalized
        settingsManager.saveAppLanguage(normalized)

        // Only apply if the effective app locale differs
        com.onlyone.app.utils.applyAppLocale(normalized)
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

        // ✅ Re-prime app language now that we have a UID
        settingsManager.getAppLanguage { saved ->
            if (saved != null) {
                val normalized = normalizeLang(saved)
                if (normalized.isNotEmpty() && normalized != _appLanguage.value) {
                    _appLanguage.value = normalized
                }
            }
            // If saved == null, leave current value as-is.
        }

        loadUser()
        refreshEngagementStatus()
    }


    // ---------------------------
    // User APIs (forwarding to UserManager)
    // ---------------------------

    fun loadUser() = userManager.loadUser()
    fun loadUser(
        checkChanged: Boolean,
        uidsToCheck: List<String>? = null
    ) = userManager.loadUser(checkChanged = checkChanged, uidsToCheck = uidsToCheck)

    fun refreshFriendDeltasIfDue(hours: Long = 6, subsetSize: Int = 12) {
        viewModelScope.launch {
            userRepository.refreshFriendDeltasIfDue(hours, subsetSize)
        }
    }

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
    fun sendFriendRequestDirect(
        targetUid: String,
        onResult: (Boolean) -> Unit
    ) = userManager.sendFriendRequestDirect(targetUid, onResult)

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

    fun saveNotificationToggles(
        message: Boolean,
        feedback: Boolean,
        friendRequests: Boolean   // 🆕 new toggle
    ) = settingsManager.saveNotificationToggles(message, feedback, friendRequests)

    fun getNotificationToggles(onResult: (Boolean, Boolean, Boolean) -> Unit) = settingsManager.getNotificationToggles(onResult)
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
                is UserRepository.FeedbackResult.Success -> _feedbackState.value =
                    FeedbackUiState.Success
                is UserRepository.FeedbackResult.AlreadySubmitted -> _feedbackState.value =
                    FeedbackUiState.AlreadySubmitted
                is UserRepository.FeedbackResult.Error -> _feedbackState.value =
                    FeedbackUiState.Error(res.message)

                else -> {}
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
