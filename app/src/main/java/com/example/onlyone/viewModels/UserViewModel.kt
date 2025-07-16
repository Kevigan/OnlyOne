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

    private val _incomingRequestUsernames = MutableStateFlow<List<String>>(emptyList())
    val incomingRequestUsernames: StateFlow<List<String>> = _incomingRequestUsernames.asStateFlow()

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

    fun loadIncomingRequestsUsernames(incomingFriendRequests: List<String>) {
        if (incomingFriendRequests.isEmpty()) {
            _incomingRequestUsernames.value = emptyList() // ✅ CLEAR it!
            Log.d("UserViewModel", "No incoming requests, usernames cleared.")
            return
        }

        userRepository.getPublicUsers(incomingFriendRequests) { publicUsers ->
            val usernames = publicUsers.map { it.username }
            _incomingRequestUsernames.value = usernames
            Log.d("UserViewModel", "Usernames loaded: $usernames")
        }
    }

    fun updateMood(mood: String) {
        _user.value?.uid?.let {
            userRepository.updateMood(it, mood)
        }
    }

    fun blockUser(blockedUid: String) {
        _user.value?.uid?.let {
            userRepository.blockUser(it, blockedUid)
        }
    }

    fun sendFriendRequestByEmail(email: String) {
        val fromUid = _user.value?.uid ?: return
        userRepository.findUserByEmail(email) { toUid ->
            if (toUid != null) {
                userRepository.sendFriendRequest(fromUid, toUid)
            } else {
                Log.w("UserViewModel", "No user found with email: $email")
            }
        }
    }

    fun acceptFriendRequest(requesterUid: String) {
        val currentUid = _user.value?.uid ?: return
        userRepository.acceptFriendRequest(currentUid, requesterUid)
            .addOnSuccessListener {
                loadUser(currentUid) // ✅ Refresh user data only after commit completes
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to accept friend request", e)
            }
    }

    fun loadFriends(friendIds: List<String>) {
        userRepository.getPublicUsers(friendIds) {
            _friends.value = it
        }
    }

}
