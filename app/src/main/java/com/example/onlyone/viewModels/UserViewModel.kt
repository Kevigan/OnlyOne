package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.repos.userRepos.UserRepository
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val friendDao: FriendDao,
    private val messageDao: MessageDao,
) : ViewModel() {
    val repository: UserRepository
        get() = userRepository

    private val _user = MutableLiveData<UserComposite?>()
    val user: LiveData<UserComposite?> get() = _user

    private val _outgoingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val outgoingRequestUsernames: StateFlow<Map<String, String>> = _outgoingRequestUsernames.asStateFlow()

    private val _incomingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val incomingRequestUsernames: StateFlow<Map<String, String>> = _incomingRequestUsernames.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<PublicUser>>(emptyList())
    val blockedUsers: StateFlow<List<PublicUser>> = _blockedUsers

    private val _engagementStatus = MutableStateFlow<UserEngagementStatus?>(null)
    val engagementStatus: StateFlow<UserEngagementStatus?> = _engagementStatus.asStateFlow()

    private val _userQueue = MutableStateFlow<List<PublicUser>>(emptyList())
    val userQueue: StateFlow<List<PublicUser>> = _userQueue.asStateFlow()

    private val _targetUser = MutableStateFlow<PublicUser?>(null)
    val targetUser: StateFlow<PublicUser?> = _targetUser.asStateFlow()

    private val _isLoadingUserBatch = MutableStateFlow(false)
    val isLoadingUserBatch: StateFlow<Boolean> = _isLoadingUserBatch.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _lastUserLoadResult = MutableStateFlow<RandomUserLoadResult?>(null)
    val lastUserLoadResult: StateFlow<RandomUserLoadResult?> = _lastUserLoadResult.asStateFlow()

    private var lastLoadedMessageUid: String? = null
    private var hasLoadedInitialBatch = false

    /*init {
        DailyResetTimer.start {
            checkAndResetSwipeLimit()
        }
    }*/

    fun loadRandomUserBatchIfNeeded(showToasts: Boolean = false) {
        if (hasLoadedInitialBatch) return
        hasLoadedInitialBatch = true
        viewModelScope.launch {
            loadRandomUserBatch(showToasts)
        }
    }

    fun setTargetUser(user: PublicUser) {
        _targetUser.value = user
    }

    fun shouldLoadMessagesFor(uid: String): Boolean {
        return if (uid != lastLoadedMessageUid) {
            lastLoadedMessageUid = uid
            true
        } else {
            false
        }
    }

    fun loadUser() {
        userRepository.fetchFullUserSession(
            onComplete = { user, friends, incoming, outgoing, blocked, engagementStatus ->
                _user.value = user
                _engagementStatus.value = engagementStatus // ✅ Set engagement state

                // Update Room if needed
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.syncFriendsToLocal(user.friendList, friends)
                }

                // Update request usernames
                _incomingRequestUsernames.value = incoming.associate { it.uid to it.username }
                _outgoingRequestUsernames.value = outgoing.associate { it.uid to it.username }

                _blockedUsers.value = blocked

                Log.d("UserViewModel", "✅ User + engagement loaded via Cloud Function")
            },
            onFailure = { error ->
                Log.e("UserViewModel", "❌ Failed to load user via Cloud Function", error)
            }
        )
    }

    fun loadFriendById(uid: String) {
        /*viewModelScope.launch {
            val user = userRepository.getPublicUser(uid)
            if (user != null) {
                _targetUser.value = user
            } else {
                _toastEvent.emit("❌ Could not load friend.")
            }
        }*/
    }

    fun updateMood(mood: String) {
        val currentUser = _user.value ?: return

        userRepository.updateMood(currentUser.uid, mood).addOnSuccessListener {
            _user.value = currentUser.copy(moodStatus = mood)
        }
    }

    /*fun updatePublicProfile(uid: String, updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.updateUserPublicProfile(uid, updates, onSuccess, onFailure)
    }*/

    fun updateChatLanguage(language: String) {
        val currentUser = _user.value ?: return
        userRepository.updateChatLanguage(currentUser.uid, language).addOnSuccessListener {
            _user.value = currentUser.copy(chatLanguage = language)
        }
    }

    fun blockUser(targetUid: String) {
        val currentUser = _user.value ?: return

        userRepository.blockAndUnfriendUser(currentUser.uid, targetUid) { success, error ->
            if (success) {
                val updatedBlockList = currentUser.blockList + targetUid
                val updatedFriendList = currentUser.friendList - targetUid
                val updatedIncoming = currentUser.incomingFriendRequests - targetUid
                val updatedOutgoing = currentUser.outgoingFriendRequests - targetUid

                _user.value = currentUser.copy(
                    blockList = updatedBlockList,
                    friendList = updatedFriendList,
                    incomingFriendRequests = updatedIncoming,
                    outgoingFriendRequests = updatedOutgoing
                )

                _incomingRequestUsernames.update { it - targetUid }
                _outgoingRequestUsernames.update { it - targetUid }

                viewModelScope.launch {
                    userRepository.removeLocalFriend(targetUid)

                    // 🔄 Now refresh public profiles for blocked tab
                    /*userRepository.getPublicUsers(updatedBlockList) { publicUsers ->
                        _blockedUsers.value = publicUsers
                    }*/
                }

                Log.d("UserViewModel", "✅ Blocked user: $targetUid")
            } else {
                Log.e("UserViewModel", "❌ Failed to block user: $error")
            }
        }
    }

    fun unblockUser(targetUid: String) {
        userRepository.unblockUser(targetUid) { success ->
            if (success) {
                _user.value = _user.value?.copy(
                    blockList = _user.value?.blockList?.filterNot { it == targetUid } ?: emptyList()
                )
                _blockedUsers.value = _blockedUsers.value.filterNot { it.uid == targetUid }

                Log.d("UserViewModel", "✅ Unblocked user: $targetUid")
            } else {
                Log.e("UserViewModel", "❌ Failed to unblock user: $targetUid")
            }
        }
    }

    fun sendFriendRequestByEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = _user.value
        val fromUid = currentUser?.uid ?: return

        if (currentUser.email == email) {
            onFailure("You can't send a request to yourself.")
            return
        }

        userRepository.findUserByEmail(email) { toUid ->
            if (toUid == null) {
                onFailure("No user found with that email.")
                return@findUserByEmail
            }

            if (currentUser.friendList.contains(toUid)) {
                onFailure("User is already your friend.")
                return@findUserByEmail
            }

            if (currentUser.outgoingFriendRequests.contains(toUid)) {
                onFailure("Friend request already sent.")
                return@findUserByEmail
            }
            Log.d("FriendRequest", "📤 Sending friend request from $fromUid to $toUid")

            // ✅ Send friend request
            userRepository.sendFriendRequest(fromUid, toUid) { success, errorMessage ->
                if (success) {
                    _user.value = currentUser.copy(
                        outgoingFriendRequests = currentUser.outgoingFriendRequests + toUid
                    )

                    userRepository.getPublicUser(toUid)
                        .addOnSuccessListener { doc ->
                            val user = doc.toObject(PublicUser::class.java)
                            if (user != null) {
                                _outgoingRequestUsernames.update { existing ->
                                    existing + (toUid to user.username)
                                }
                            }
                            onSuccess()
                        }
                        .addOnFailureListener {
                            Log.e("FriendRequest", "⚠️ Failed to fetch user after sending request", it)
                            onSuccess() // Still call success even if username couldn't be resolved
                        }
                } else {
                    onFailure(errorMessage ?: "Unknown error")
                }
            }
        }
    }

    fun sendFriendRequestDirect(targetUid: String) {
        val currentUser = _user.value ?: return

        // Already a friend?
        if (currentUser.friendList.contains(targetUid)) {
            Log.w("FriendRequest", "🚫 Already friends with $targetUid")
            return
        }

        // Already sent?
        if (currentUser.outgoingFriendRequests.contains(targetUid)) {
            Log.w("FriendRequest", "🚫 Friend request already sent to $targetUid")
            return
        }

        userRepository.sendFriendRequest(
            fromUid = currentUser.uid,
            toUid = targetUid
        ) { success, errorMessage ->
            if (success) {
                Log.d("FriendRequest", "✅ Sent friend request to $targetUid")
                _user.value = currentUser.copy(
                    outgoingFriendRequests = currentUser.outgoingFriendRequests + targetUid
                )
            } else {
                Log.e("FriendRequest", "❌ Failed to send request: $errorMessage")
            }
        }
    }

    fun cancelOutgoingFriendRequest(
        targetUid: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val currentUser = _user.value ?: return

        userRepository.cancelOutgoingFriendRequest(currentUser.uid, targetUid) { success, error ->
            if (success) {
                val updatedOutgoing = currentUser.outgoingFriendRequests - targetUid
                _user.value = currentUser.copy(outgoingFriendRequests = updatedOutgoing)

                _outgoingRequestUsernames.update { it - targetUid }

                onSuccess()
            } else {
                Log.e("UserViewModel", "❌ Failed to cancel outgoing request: $error")
                onFailure()
            }
        }
    }

    fun acceptFriendRequest(requesterUid: String) {
        val currentUser = _user.value ?: return

        userRepository.acceptFriendRequest(currentUser.uid, requesterUid) { success, error ->
            if (success) {
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                val updatedFriendList = currentUser.friendList + requesterUid

                _user.value = currentUser.copy(
                    incomingFriendRequests = updatedRequests,
                    friendList = updatedFriendList
                )

                _incomingRequestUsernames.update { it - requesterUid }

                viewModelScope.launch(Dispatchers.IO) {
                    val fullFriendList = _user.value?.friendList ?: emptyList()
                    /*val allPublicUsers = userRepository.getPublicUsersSuspend(fullFriendList)
                    userRepository.syncFriendsToLocal(fullFriendList, allPublicUsers)*/
                }
            } else {
                Log.e("FriendAccept", "❌ Failed to accept request: $error")
            }
        }
    }

    fun deleteFriend(friendUid: String) {
        val currentUser = _user.value ?: return

        userRepository.deleteFriend(currentUser.uid, friendUid) { success, error ->
            if (success) {
                val updatedFriendList = currentUser.friendList - friendUid
                _user.value = currentUser.copy(friendList = updatedFriendList)

                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.removeLocalFriend(friendUid)
                }

                Log.d("UserViewModel", "✅ Removed friend: $friendUid")
            } else {
                Log.e("UserViewModel", "❌ Failed to remove friend: $error")
            }
        }
    }

    fun declineFriendRequest(requesterUid: String) {
        val currentUser = _user.value ?: return

        userRepository.declineFriendRequest(currentUser.uid, requesterUid) { success, error ->
            if (success) {
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                _user.value = currentUser.copy(incomingFriendRequests = updatedRequests)
                _incomingRequestUsernames.update { it - requesterUid }
            } else {
                Log.e("FriendDecline", "❌ Failed to decline request: $error")
            }
        }
    }

    fun upgradeMaxMessageLength(
        levels: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.upgradeMaxMessageLength(
            levels = levels,
            onSuccess = { _, _ ->
                loadUser() // refresh user data from Firestore
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    fun updateNotificationPreference(
        key: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val currentUser = _user.value ?: return
        userRepository.updateNotificationSetting(
            uid = currentUser.uid,
            key = key,
            enabled = enabled,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }


    ///////RandomUsers///////////
    suspend fun loadRandomUserBatch(showToasts: Boolean = false): RandomUserLoadResult {
        _isLoadingUserBatch.value = true

        val result = try {
            val currentUser = _user.value
            val engagement = _engagementStatus.value

            if (currentUser == null || engagement == null) {
                val fallback = RandomUserLoadResult.NoSwipesLeft
                _lastUserLoadResult.value = fallback
                return fallback
            }

            val swipesLeft = currentUser.maxSwipes - engagement.swipesUsed
            if (swipesLeft <= 0) {
                if (showToasts) _toastEvent.emit("🚫 No swipes left today.")
                val noSwipes = RandomUserLoadResult.NoSwipesLeft
                _lastUserLoadResult.value = noSwipes
                return noSwipes
            }

            val writtenToday = userRepository.observeWrittenToday().first().map { it.receiverId }
            val searchLanguage = userRepository.getSearchUserLanguage(currentUser.uid)

            val randomUsers = userRepository.loadRandomUserBatchSuspend(
                excludedIds = writtenToday,
                chatLanguage = searchLanguage
            )

            if (randomUsers.isNotEmpty()) {
                _userQueue.value = randomUsers
                _targetUser.value = randomUsers.first()
                RandomUserLoadResult.Success.also { _lastUserLoadResult.value = it }
            } else {
                _userQueue.value = emptyList()
                _targetUser.value = null

                if (showToasts) {
                    if (searchLanguage != "any") {
                        _toastEvent.emit("No users found in selected language.")
                    } else {
                        _toastEvent.emit("🎉 You've seen everyone for now.")
                    }
                }

                RandomUserLoadResult.NoUsersFound.also { _lastUserLoadResult.value = it }
            }

        } finally {
            _isLoadingUserBatch.value = false
        }

        return result
    }

    fun consumeNextUserFromQueue() {
        val user = _user.value
        val engagement = _engagementStatus.value

        if (user == null || engagement == null) return

        viewModelScope.launch {
            val swipesLeft = user.maxSwipes - engagement.swipesUsed
            Log.d("UserViewModel", "🧮 swipesLeft=${swipesLeft}")
            if (swipesLeft <= 0) {
                _toastEvent.emit("🚫 No swipes left today.")
                return@launch
            }

            val desiredLanguage = userRepository.getSearchUserLanguage(user.uid)
            val remainingUsers = _userQueue.value.drop(1)
            val nextMatch = remainingUsers.firstOrNull {
                it.chatLanguage == desiredLanguage || desiredLanguage == "any"
            }

            Log.e("UserViewModel", "❌ engagement=${engagement.swipesUsed}")
            Log.e("UserViewModel", "❌ desiredLanguage=$desiredLanguage")
            Log.e("UserViewModel", "❌ remainingUsers=${remainingUsers.size}")

            // ✅ Call Cloud Function to increment swipe
            val success = userRepository.incrementSwipeCount()
            if (!success) {
                Log.e("UserViewModel", "❌ Failed to increment swipe count")
            }

            // ✅ Locally increment swipesUsed
            _engagementStatus.value = engagement.copy(
                swipesUsed = engagement.swipesUsed + 1
            )

            if (_engagementStatus.value?.swipesUsed == user.maxSwipes) {
                Log.d("UserViewModel", "🔁 refreshEngagementStatus...")
                refreshEngagementStatus()
            }

            if (nextMatch != null) {
                _userQueue.value = remainingUsers
                _targetUser.value = nextMatch
            } else {
                _userQueue.value = emptyList()
                _targetUser.value = null

                Log.d("UserViewModel", "🔁 Queue empty, loading new batch...")

                when (val result = loadRandomUserBatch()) {
                    is RandomUserLoadResult.NoUsersFound -> {
                        _toastEvent.emit("🎉 You've seen everyone for now.")
                    }
                    is RandomUserLoadResult.NoSwipesLeft -> {
                        _toastEvent.emit("🚫 You've hit your daily swipe limit.")
                    }
                    is RandomUserLoadResult.Success -> {
                        // silently succeeds
                    }
                }
            }
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


///////RandomUsers End///////////

    fun observeLocalFriends(): Flow<List<LocalFriend>> {
        return friendDao.getAllFriends()
    }

    suspend fun getLocalFriend(uid: String): LocalFriend? {
        return userRepository.getLocalFriend(uid)
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

    fun saveAppLanguage(language: String) {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            userRepository.saveAppLanguage(currentUser.uid, language)
        }
    }

    fun getAppLanguage(onResult: (String) -> Unit) {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            val lang = userRepository.getAppLanguage(currentUser.uid)
            onResult(lang)
        }
    }

    fun saveSearchUserLanguage(lang: String) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            userRepository.saveSearchUserLanguage(uid, lang)
        }
    }

    fun getSearchUserLanguage(onResult: (String) -> Unit) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            val lang = userRepository.getSearchUserLanguage(uid)
            onResult(lang)
        }
    }

    fun saveNotificationToggles(message: Boolean, feedback: Boolean) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            userRepository.saveLocalNotificationSettings(uid, message, feedback)
        }
    }

    fun getNotificationToggles(onResult: (Boolean, Boolean) -> Unit) {
        val uid = _user.value?.uid ?: return
        viewModelScope.launch {
            val (message, feedback) = userRepository.getLocalNotificationSettings(uid)
            onResult(message, feedback)
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

    sealed class RandomUserLoadResult {
        object Success : RandomUserLoadResult()
        object NoUsersFound : RandomUserLoadResult()
        object NoSwipesLeft : RandomUserLoadResult()
    }


}
