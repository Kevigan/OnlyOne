package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dao.FriendDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.example.onlyone.data.UserSwipeStatus
import com.example.onlyone.repos.UserRepository
import com.example.onlyone.utils.DailyResetTimer
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val friendDao: FriendDao
) : ViewModel() {
    val repository: UserRepository
        get() = userRepository

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _outgoingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val outgoingRequestUsernames: StateFlow<Map<String, String>> = _outgoingRequestUsernames.asStateFlow()

    private val _incomingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val incomingRequestUsernames: StateFlow<Map<String, String>> = _incomingRequestUsernames.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<PublicUser>>(emptyList())
    val blockedUsers: StateFlow<List<PublicUser>> = _blockedUsers

    private val _swipeStatus = MutableStateFlow<UserSwipeStatus?>(null)
    val swipeStatus: StateFlow<UserSwipeStatus?> = _swipeStatus.asStateFlow()


    private var lastLoadedMessageUid: String? = null

    init {
        DailyResetTimer.start {
            checkAndResetSwipeLimit()
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

    fun loadUser() {
        userRepository.getUserWithFriends(
            onComplete = { user, friends, incoming, outgoing, blocked ->
                _user.value = user

                // Update Room if needed
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.syncFriendsToLocal(user.friendList, friends)
                }

                // Update request usernames
                _incomingRequestUsernames.value = incoming.associate { it.uid to it.username }
                _outgoingRequestUsernames.value = outgoing.associate { it.uid to it.username }

                // ✅ Update blocked list
                _blockedUsers.value = blocked

                Log.d("UserViewModel", "✅ User loaded via Cloud Function")
            },
            onFailure = { error ->
                Log.e("UserViewModel", "❌ Failed to load user via Cloud Function", error)
            }
        )
    }

    fun loadSwipeStatus(uid: String) {
        userRepository.getSwipeStatus(uid) { status ->
            _swipeStatus.value = status
        }
    }

    fun checkAndResetSwipeLimit() {
        val uid = _user.value?.uid ?: return

        userRepository.maybeResetSwipes { reset ->
            Log.d("SwipeReset", if (reset) "✅ Swipes reset for today" else "ℹ️ No reset needed")

            // Always re-sync swipe data from Firestore, even if no reset
            userRepository.syncSwipeStatusFromCloud(uid) {
                loadSwipeStatus(uid) // updates UI
            }
        }
    }

    fun updateMood(mood: String) {
        val currentUser = _user.value ?: return

        userRepository.updateMood(currentUser.uid, mood).addOnSuccessListener {
            _user.value = currentUser.copy(moodStatus = mood)
        }
    }

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
                    userRepository.getPublicUsers(updatedBlockList) { publicUsers ->
                        _blockedUsers.value = publicUsers
                    }
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

                    userRepository.getPublicUsers(listOf(toUid)) { users ->
                        val user = users.firstOrNull()
                        if (user != null) {
                            _outgoingRequestUsernames.update { existing ->
                                existing + (toUid to user.username)
                            }
                        }
                        onSuccess()
                    }
                } else {
                    Log.e("FriendRequest", "❌ sendFriendRequest failed: $errorMessage")
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
                    val allPublicUsers = userRepository.getPublicUsersSuspend(fullFriendList)
                    userRepository.syncFriendsToLocal(fullFriendList, allPublicUsers)
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

    fun updateNotificationPreference(key: String, enabled: Boolean) {
        val currentUser = _user.value ?: return
        userRepository.updateNotificationSetting(
            uid = currentUser.uid,
            key = key,
            enabled = enabled
        )
    }


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

}
