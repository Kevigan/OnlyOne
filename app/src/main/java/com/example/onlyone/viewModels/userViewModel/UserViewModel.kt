package com.example.onlyone.viewModels.userViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.onlyone.ads.AdCounter
import com.example.onlyone.cloudMessaging.MessageNotifier
import com.example.onlyone.data.LocalFavoriteMessage
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.data.achievementDefinitions.MessageAchievements
import com.example.onlyone.repos.userRepos.UserRepository
import com.google.firebase.auth.FirebaseAuth // ✅ NEW
import com.google.firebase.auth.FirebaseUser // ✅ NEW
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

@HiltViewModel
class UserViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val friendDao: FriendDao,
    private val messageDao: MessageDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    val repository: UserRepository
        get() = userRepository

    private val _user = MutableLiveData<UserComposite?>()
    val user: LiveData<UserComposite?> get() = _user

    val incomingRequestUsernames: StateFlow<Map<String, String>> get() = userManager.incomingRequestUsernames
    val outgoingRequestUsernames: StateFlow<Map<String, String>> get() = userManager.outgoingRequestUsernames
    val blockedUsers: StateFlow<List<PublicUser>> get() = userManager.blockedUsers

    private val _engagementStatus = MutableStateFlow<UserEngagementStatus?>(null)
    val engagementStatus: StateFlow<UserEngagementStatus?> = _engagementStatus.asStateFlow()

    val userQueue get() = swipeManager.userQueue
    val targetUser get() = swipeManager.targetUser
    val isLoadingUserBatch get() = swipeManager.isLoadingUserBatch
    val lastUserLoadResult get() = swipeManager.lastUserLoadResult
    val toastEvent get() = swipeManager.toastEvent

    private var lastLoadedMessageUid: String? = null
    private var hasLoadedInitialBatch = false

    // ✅ NEW — keep track of which uid we last hydrated to avoid duplicate work
    private var lastUidLoaded: String? = null

    // ✅ NEW — Firebase Auth wiring
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val current = firebaseAuth.currentUser
        onAuthUserChanged(current)
    }

    private val adCounter = AdCounter(appContext)

    val adCountToday = adCounter.countToday   // Flow<Int>
    val showAdFlow = adCountToday.map { it > 0 && it % 10 == 0 }  // every 10th

    fun onRandomCardVisible() {
        viewModelScope.launch { adCounter.increment() }
    }

    var userManager: UserManager
    init {
        userManager = UserManager(
            userRepository,
            viewModelScope,
            updateUser = { _user.value = it },
            updateEngagementStatus = { _engagementStatus.value = it },
            getUser = { _user.value }
        )

        // ✅ NEW — start listening to auth and prime once
        auth.addAuthStateListener(authListener)
        onAuthUserChanged(auth.currentUser) // prime immediately in case listener is late
    }

    // ✅ NEW — central handler whenever Firebase auth user changes (including app resume)
    private fun onAuthUserChanged(current: FirebaseUser?) {
        if (current == null) {
            lastUidLoaded = null
            _user.postValue(null)
            return
        }
        if (current.uid == lastUidLoaded && _user.value != null) {
            // Already loaded for this uid; nothing to do.
            return
        }
        lastUidLoaded = current.uid
        // Your UserManager already pushes into _user via updateUser; just trigger it.
        loadUser()
        // Optionally re-warm engagement too, if needed:
        refreshEngagementStatus()
    }

    fun loadUser() { userManager.loadUser() }

    fun updatePublicProfile(updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) { userManager.updatePublicProfile(updates, onSuccess, onFailure) }
    fun blockUser(targetUid: String) { userManager.blockUser(targetUid) }
    fun unblockUser(targetUid: String) { userManager.unblockUser(targetUid) }
    fun sendFriendRequestByEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) { userManager.sendFriendRequestByEmail(email, onSuccess, onFailure) }
    fun sendFriendRequestDirect(targetUid: String) { userManager.sendFriendRequestDirect(targetUid) }
    fun cancelOutgoingFriendRequest(targetUid: String, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) { userManager.cancelOutgoingFriendRequest(targetUid, onSuccess, onFailure) }
    fun acceptFriendRequest(requesterUid: String) { userManager.acceptFriendRequest(requesterUid) }
    fun declineFriendRequest(requesterUid: String) { userManager.declineFriendRequest(requesterUid) }
    fun deleteFriend(friendUid: String) { userManager.deleteFriend(friendUid) }
    fun fetchUserInventory() { userManager.fetchUserInventory() }
     fun clearFavouriteMessage(onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) { userManager.clearFavouriteMessage(onSuccess, onFailure) }
    // ADD these one-liners next to your existing ones:
    fun setFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) { userManager.setFavouriteMessage(fav, onSuccess, onFailure) }

    fun toggleFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) { userManager.toggleFavouriteMessage(fav, onSuccess, onFailure) }


    var upgradeManager: UpgradeManager
    init {
        upgradeManager = UpgradeManager(
            userRepository,
            viewModelScope,
            loadUser = { loadUser() }
        )
    }
    fun upgradeFeature(feature: String, levels: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) { upgradeManager.upgradeFeature(feature, levels, onSuccess, onFailure) }

    var settingsManager: UserSettingsManager
    init {
        settingsManager = UserSettingsManager(
            userRepository,
            viewModelScope,
            getUid = { _user.value?.uid }
        )
    }
    fun updateNotificationPreference(key: String, enabled: Boolean, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) { settingsManager.updateNotificationPreference(key, enabled, onSuccess, onFailure) }
    fun saveAppLanguage(language: String) { settingsManager.saveAppLanguage(language) }
    fun getAppLanguage(onResult: (String) -> Unit) { settingsManager.getAppLanguage(onResult) }
    fun saveNotificationToggles(message: Boolean, feedback: Boolean) { settingsManager.saveNotificationToggles(message, feedback) }
    fun getNotificationToggles(onResult: (Boolean, Boolean) -> Unit) { settingsManager.getNotificationToggles(onResult) }
    fun saveSearchUserLanguage(lang: String) { settingsManager.saveSearchUserLanguage(lang) }
    fun getSearchUserLanguage(onResult: (String) -> Unit) { settingsManager.getSearchUserLanguage(onResult) }

    var swipeManager: SwipeManager
    init {
        swipeManager = SwipeManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value },
            getEngagement = { _engagementStatus.value },
            updateEngagement = { _engagementStatus.value = it },
            refreshEngagementStatus = { refreshEngagementStatus() }
        )
    }
    fun loadRandomUserBatchIfNeeded(showToasts: Boolean = false) = swipeManager.loadRandomUserBatchIfNeeded(showToasts)
    suspend fun loadRandomUserBatch(showToasts: Boolean = false): SwipeManager.RandomUserLoadResult { return swipeManager.loadRandomUserBatch(showToasts) }
    fun consumeNextUserFromQueue() = swipeManager.consumeNextUserFromQueue()
    fun setTargetUser(user: PublicUser) { swipeManager.setTargetUser(user) }

    var shopManager: ShopManager
    init {
        shopManager = ShopManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value },
            updateUser = { _user.value = it }
        )
    }
    fun buyAvatar(avatarId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) { shopManager.buyAvatar(avatarId, onSuccess, onFailure) }
    fun buyMood(moodId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) { shopManager.buyMood(moodId, onSuccess, onFailure) }
    fun buyTheme(themeId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit){shopManager.buyTheme(themeId, onSuccess, onFailure)}

    var achievementManager: AchievementManager
    init {
        achievementManager = AchievementManager(
            userRepository,
            viewModelScope,
            getUser = { _user.value }
        )
    }
    fun loadAchievementsWithStats() {
        // Skip fetching definitions from server – we already have MessageAchievements.definitions
        achievementManager.loadUserAchievementsWithStats(
            onSuccess = {
                updateGroupedAchievements()
            },
            onFailure = { error ->
                Log.e("Achievements", "❌ Failed to load: $error")
            }
        )
    }

    private val _groupedAchievements = MutableLiveData<Map<String, List<AchievementManager.AchievementWithProgress>>>()
    val groupedAchievements: LiveData<Map<String, List<AchievementManager.AchievementWithProgress>>> = _groupedAchievements

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

    init {
        viewModelScope.launch {
            MessageNotifier.newMessageFlow.collect { (title, body) ->
                if (title?.contains("feedback", ignoreCase = true) == true) {
                    fetchUserInventory()
                }
            }
        }
    }

    fun shouldLoadMessagesFor(uid: String): Boolean {
        return if (uid != lastLoadedMessageUid) {
            lastLoadedMessageUid = uid
            true
        } else {
            false
        }
    }

    fun refreshEngagementStatus() {
        val uid = _user.value?.uid ?: return

        userRepository.fetchEngagementStatus(uid) { status ->
            if (status != null) {
                _engagementStatus.value = status
                Log.d("UserViewModel", "✅ EngagementStatus refreshed")
            }
        }
    }

    fun observeLocalFriends(): Flow<List<LocalFriend>> {
        return friendDao.getAllFriends()
    }

    fun hardResetFriends(){
        viewModelScope.launch {
            userRepository.hardResetFriends()
        }
    }

    fun hardResetLocalMessages(){
        viewModelScope.launch {
            userRepository.hardResetLocalMessages()
        }
    }

    ///////debug///////
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

    override fun onCleared() {
        // ✅ NEW — remove listener to avoid leaks
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }
}
