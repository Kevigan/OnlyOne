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
import com.example.onlyone.repos.UserRepository
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

    private var lastLoadedMessageUid: String? = null

    fun shouldLoadMessagesFor(uid: String): Boolean {
        return if (uid != lastLoadedMessageUid) {
            lastLoadedMessageUid = uid
            true
        } else {
            false
        }
    }

    fun createUserProfile(uid: String, email: String, username: String): Task<Void> {
        return userRepository.createUserProfile(uid, email, username)
    }

    fun loadUser() {
        userRepository.getUserWithFriends(
            onComplete = { user, friends, incoming, outgoing ->
                _user.value = user

                // Update Room if needed
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.syncFriendsToLocal(user.friendList, friends)
                }

                // Update request usernames
                _incomingRequestUsernames.value = incoming.associate { it.uid to it.username }
                _outgoingRequestUsernames.value = outgoing.associate { it.uid to it.username }

                Log.d("UserViewModel", "✅ User loaded via Cloud Function")
            },
            onFailure = { error ->
                Log.e("UserViewModel", "❌ Failed to load user via Cloud Function", error)
            }
        )
    }

    fun updateMood(mood: String) {
        val currentUser = _user.value ?: return

        userRepository.updateMood(currentUser.uid, mood).addOnSuccessListener {
            _user.value = currentUser.copy(moodStatus = mood)
        }
    }

    fun blockUser(blockedUid: String) {
        _user.value?.uid?.let {
            userRepository.blockUser(it, blockedUid)
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

            // ✅ Send friend request
            userRepository.sendFriendRequest(fromUid, toUid).addOnSuccessListener {
                _user.value = currentUser.copy(
                    outgoingFriendRequests = currentUser.outgoingFriendRequests + toUid
                )

                // ✅ Now fetch username and update map
                userRepository.getPublicUsers(listOf(toUid)) { users ->
                    val user = users.firstOrNull()
                    if (user != null) {
                        _outgoingRequestUsernames.update { existing ->
                            existing + (toUid to user.username)
                        }
                    } else {
                        Log.w("FriendRequest", "⚠️ Could not fetch PublicUser for $toUid")
                    }
                    onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e("FriendRequest", "❌ Failed to send friend request", e)
                onFailure("Firestore error: ${e.message}")
            }
        }
    }

    fun cancelOutgoingFriendRequest(
        targetUid: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val currentUser = _user.value ?: return

        userRepository.cancelOutgoingFriendRequest(currentUser.uid, targetUid)
            .addOnSuccessListener {
                val updatedOutgoing = currentUser.outgoingFriendRequests - targetUid
                _user.value = currentUser.copy(outgoingFriendRequests = updatedOutgoing)

                _outgoingRequestUsernames.update { it - targetUid }

                onSuccess() // ✅ Notify success
            }
            .addOnFailureListener {
                Log.w("UserViewModel", "Failed to cancel outgoing request to $targetUid", it)
                onFailure()
            }
    }

    fun acceptFriendRequest(requesterUid: String) {
        val currentUser = _user.value ?: return

        userRepository.acceptFriendRequest(currentUser.uid, requesterUid)
            .addOnSuccessListener {
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                val updatedFriendList = currentUser.friendList + requesterUid

                _user.value = currentUser.copy(
                    incomingFriendRequests = updatedRequests,
                    friendList = updatedFriendList
                )

                _incomingRequestUsernames.update { it - requesterUid }

                viewModelScope.launch(Dispatchers.IO) {
                    val publicUsers = userRepository.getPublicUsersSuspend(listOf(requesterUid))
                    userRepository.syncFriendsToLocal(updatedFriendList, publicUsers)
                }
            }
    }

    fun deleteFriend(friendUid: String) {
        val currentUser = _user.value ?: return

        userRepository.deleteFriend(currentUser.uid, friendUid)
            .addOnSuccessListener {
                val updatedFriendList = currentUser.friendList - friendUid
                _user.value = currentUser.copy(friendList = updatedFriendList)

                // Remove from local Room DB
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.removeLocalFriend(friendUid)
                }

                Log.d("UserViewModel", "✅ Removed friend: $friendUid")
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "❌ Failed to remove friend: $friendUid", e)
            }
    }


    fun declineFriendRequest(requesterUid: String) {
        val currentUser = _user.value ?: return

        userRepository.declineFriendRequest(currentUser.uid, requesterUid)
            .addOnSuccessListener {
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                _user.value = currentUser.copy(incomingFriendRequests = updatedRequests)

                _incomingRequestUsernames.update { it - requesterUid }
            }
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
}
