package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.example.onlyone.repos.UserRepository
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val repository: UserRepository
        get() = userRepository

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _friends = MutableStateFlow<List<PublicUser>>(emptyList())
    val friends: StateFlow<List<PublicUser>> = _friends.asStateFlow()

    private val _outgoingRequestUsernames = MutableStateFlow<List<String>>(emptyList())
    val outgoingRequestUsernames: StateFlow<List<String>> = _outgoingRequestUsernames.asStateFlow()

    private val _incomingRequestUsernames = MutableStateFlow<List<String>>(emptyList())
    val incomingRequestUsernames: StateFlow<List<String>> = _incomingRequestUsernames.asStateFlow()

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

    fun loadUser(uid: String) {
        //Log.d("UserViewModel", "Loading user with UID: $uid")
        userRepository.getFullUser(uid) { loadedUser ->
            if (loadedUser != null) {
                //Log.d("UserViewModel", "User loaded: ${loadedUser.username} (${loadedUser.uid})")
                Log.d("UserViewModel", "Incoming Friend Requests: ${loadedUser.incomingFriendRequests.size}")
                Log.d("UserViewModel", "Incoming Friend Requests2: ${_incomingRequestUsernames.value}")
                //Log.d("UserViewModel", "User email: ${loadedUser.email}")
            } else {
                //Log.w("UserViewModel", "Failed to load user for UID: $uid")
            }
            _user.value = loadedUser
            if (loadedUser != null) {
                loadFriends(loadedUser.friendList)
                loadIncomingRequestsUsernames(loadedUser.incomingFriendRequests)
                //Log.d("UserViewModel", "User name: ${_incomingRequestUsernames.value}")
            }
            if (loadedUser != null) {
                Log.d("UserViewModel", "Incoming Friend Requests: ${loadedUser.incomingFriendRequests.size}")
            }
            Log.d("UserViewModel", "Incoming Friend Requests2: ${_incomingRequestUsernames.value}")
        }
    }

    fun loadIncomingRequestsUsernames(incomingFriendRequests: List<String>, append: Boolean = false) {
        if (incomingFriendRequests.isEmpty()) {
            if (!append) {
                _incomingRequestUsernames.value = emptyList() // Only clear if replacing
            }
            return
        }

        userRepository.getPublicUsers(incomingFriendRequests) { publicUsers ->
            val newUsernames = publicUsers.map { it.username }

            if (append) {
                _incomingRequestUsernames.update { existing ->
                    val new = newUsernames.filterNot { it in existing }
                    existing + new
                }
            } else {
                _incomingRequestUsernames.value = newUsernames
            }
        }
    }

    fun loadOutgoingRequestUsernames(uids: List<String>, append: Boolean = false) {
        if (uids.isEmpty()) {
            if (!append) _outgoingRequestUsernames.value = emptyList()
            return
        }

        userRepository.getPublicUsers(uids) { users ->
            val usernames = users.map { it.username }

            if (append) {
                _outgoingRequestUsernames.update { existing ->
                    val new = usernames.filterNot { it in existing }
                    existing + new
                }
            } else {
                _outgoingRequestUsernames.value = usernames
            }
        }
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
        onFailure: (String) -> Unit // Changed to pass error reason
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

                loadOutgoingRequestUsernames(listOf(toUid), append = true)
                onSuccess()
            }.addOnFailureListener {
                onFailure("Failed to send friend request.")
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

                loadOutgoingRequestUsernames(updatedOutgoing)
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
                // ✅ Patch local state (no fetch from Firestore)
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                val updatedFriendList = currentUser.friendList + requesterUid

                _user.value = currentUser.copy(
                    incomingFriendRequests = updatedRequests,
                    friendList = updatedFriendList
                )

                // ✅ Trigger downstream recomposition
                loadIncomingRequestsUsernames(updatedRequests)
                loadFriends(listOf(requesterUid), append = true)
            }
    }

    fun declineFriendRequest(requesterUid: String) {
        val currentUser = _user.value ?: return

        userRepository.declineFriendRequest(currentUser.uid, requesterUid)
            .addOnSuccessListener {
                // ✅ Patch local state
                val updatedRequests = currentUser.incomingFriendRequests - requesterUid
                _user.value = currentUser.copy(incomingFriendRequests = updatedRequests)

                // ✅ Update UI state
                loadIncomingRequestsUsernames(updatedRequests)
            }
    }

    fun loadFriends(friendIds: List<String>, append: Boolean = false) {
        if (friendIds.isEmpty()) {
            if (!append) {
                _friends.value = emptyList() // clear fully only on replace
            }
            return
        }

        userRepository.getPublicUsers(friendIds) { newFriends ->
            if (append) {
                _friends.update { existingFriends ->
                    // Only add friends who aren't already in the list
                    val new = newFriends.filterNot { nf -> existingFriends.any { it.uid == nf.uid } }
                    existingFriends + new
                }
            } else {
                _friends.value = newFriends
            }
        }
    }
}
