package com.example.onlyone.viewModels.userViewModel

import android.util.Log
import com.example.onlyone.data.LocalFavoriteMessage
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserManager @Inject constructor(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val updateUser: (UserComposite) -> Unit,
    private val updateEngagementStatus: (UserEngagementStatus) -> Unit,
    private val getUser: () -> UserComposite?
) {
    private val _incomingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val incomingRequestUsernames: StateFlow<Map<String, String>> = _incomingRequestUsernames.asStateFlow()

    private val _outgoingRequestUsernames = MutableStateFlow<Map<String, String>>(emptyMap())
    val outgoingRequestUsernames: StateFlow<Map<String, String>> = _outgoingRequestUsernames.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<PublicUser>>(emptyList())
    val blockedUsers: StateFlow<List<PublicUser>> = _blockedUsers.asStateFlow()

    fun loadUser() {
        userRepository.fetchFullUserSession(
            onComplete = { user, friends, incoming, outgoing, blocked, engagementStatus ->
                updateUser(user)
                updateEngagementStatus(engagementStatus)

                _incomingRequestUsernames.value = incoming.associate { it.uid to it.username }
                _outgoingRequestUsernames.value = outgoing.associate { it.uid to it.username }
                _blockedUsers.value = blocked

                viewModelScope.launch {
                    userRepository.syncFriendsToLocal(user.friendList, friends)
                }

                Log.d("UserSessionManager", "✅ User session loaded successfully")
            },
            onFailure = { error ->
                Log.e("UserSessionManager", "❌ Failed to load user", error)
            }
        )
    }

    fun updatePublicProfile(
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = getUser() ?: return

        viewModelScope.launch {
            userRepository.updatePublicProfileSecure(
                updates = updates,
                onSuccess = {
                    val updated = currentUser.copy(
                        moodStatus = updates["moodStatus"] as? String ?: currentUser.moodStatus,
                        chatLanguage = updates["chatLanguage"] as? String ?: currentUser.chatLanguage,
                        avatarId = (updates["avatarId"] as? Number)?.toInt() ?: currentUser.avatarId,
                        moodId   = (updates["moodId"]   as? Number)?.toInt() ?: currentUser.moodId,

                        // 🆕 public extras
                        gender = updates["gender"] as? String ?: currentUser.gender,
                        age = (updates["age"] as? Number)?.toInt() ?: currentUser.age,
                        city = updates["city"] as? String ?: currentUser.city,
                        username = updates["username"] as? String ?: currentUser.username
                    )
                    updateUser(updated)   // triggers Compose recomposition
                    onSuccess()
                },
                onFailure = { e -> onFailure(e.message ?: "Update failed") }
            )
        }
    }

    fun blockUser(targetUid: String) {
        val currentUser = getUser() ?: return

        userRepository.blockAndUnfriendUser(currentUser.uid, targetUid) { success, error ->
            if (success) {
                val updatedUser = currentUser.copy(
                    blockList = currentUser.blockList + targetUid,
                    friendList = currentUser.friendList - targetUid,
                    incomingFriendRequests = currentUser.incomingFriendRequests - targetUid,
                    outgoingFriendRequests = currentUser.outgoingFriendRequests - targetUid
                )
                updateUser(updatedUser)

                _incomingRequestUsernames.update { it - targetUid }
                _outgoingRequestUsernames.update { it - targetUid }

                viewModelScope.launch {
                    userRepository.removeLocalFriend(targetUid)
                }

                Log.d("UserFriendManager", "✅ Blocked user: $targetUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to block user: $error")
            }
        }
    }

    fun unblockUser(targetUid: String) {
        val currentUser = getUser() ?: return
        userRepository.unblockUser(targetUid) { success ->
            if (success) {
                val updated = currentUser.copy(
                    blockList = currentUser.blockList.filterNot { it == targetUid }
                )
                updateUser(updated)
                _blockedUsers.value = _blockedUsers.value.filterNot { it.uid == targetUid }

                Log.d("UserFriendManager", "✅ Unblocked user: $targetUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to unblock user: $targetUid")
            }
        }
    }

    fun sendFriendRequestByEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = getUser() ?: return

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

            userRepository.sendFriendRequest(currentUser.uid, toUid) { success, errorMessage ->
                if (success) {
                    val updated = currentUser.copy(
                        outgoingFriendRequests = currentUser.outgoingFriendRequests + toUid
                    )
                    updateUser(updated)

                    userRepository.getPublicUser(toUid)
                        .addOnSuccessListener { doc ->
                            doc.toObject(PublicUser::class.java)?.let { publicUser ->
                                _outgoingRequestUsernames.update {
                                    it + (toUid to publicUser.username)
                                }
                            }
                            onSuccess()
                        }
                        .addOnFailureListener {
                            Log.e("UserFriendManager", "⚠️ Username fetch failed", it)
                            onSuccess()
                        }
                } else {
                    onFailure(errorMessage ?: "Unknown error")
                }
            }
        }
    }

    fun sendFriendRequestDirect(targetUid: String) {
        val currentUser = getUser() ?: return

        if (currentUser.friendList.contains(targetUid) ||
            currentUser.outgoingFriendRequests.contains(targetUid)) return

        userRepository.sendFriendRequest(currentUser.uid, targetUid) { success, errorMessage ->
            if (success) {
                updateUser(currentUser.copy(
                    outgoingFriendRequests = currentUser.outgoingFriendRequests + targetUid
                ))
                Log.d("UserFriendManager", "✅ Sent request to $targetUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to send request: $errorMessage")
            }
        }
    }

    fun cancelOutgoingFriendRequest(
        targetUid: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val currentUser = getUser() ?: return

        userRepository.cancelOutgoingFriendRequest(currentUser.uid, targetUid) { success, error ->
            if (success) {
                updateUser(currentUser.copy(
                    outgoingFriendRequests = currentUser.outgoingFriendRequests - targetUid
                ))
                _outgoingRequestUsernames.update { it - targetUid }
                onSuccess()
            } else {
                Log.e("UserFriendManager", "❌ Failed to cancel request: $error")
                onFailure()
            }
        }
    }

    fun acceptFriendRequest(requesterUid: String) {
        val currentUser = getUser() ?: return

        userRepository.acceptFriendRequest(currentUser.uid, requesterUid) { success, error ->
            if (success) {
                val updated = currentUser.copy(
                    incomingFriendRequests = currentUser.incomingFriendRequests - requesterUid,
                    friendList = currentUser.friendList + requesterUid
                )
                updateUser(updated)
                _incomingRequestUsernames.update { it - requesterUid }

                viewModelScope.launch {
                    val fullList = updated.friendList
                    // You may call getPublicUsersSuspend() here if you want to re-sync
                }

                Log.d("UserFriendManager", "✅ Accepted friend request from $requesterUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to accept request: $error")
            }
        }
    }

    fun declineFriendRequest(requesterUid: String) {
        val currentUser = getUser() ?: return

        userRepository.declineFriendRequest(currentUser.uid, requesterUid) { success, error ->
            if (success) {
                updateUser(currentUser.copy(
                    incomingFriendRequests = currentUser.incomingFriendRequests - requesterUid
                ))
                _incomingRequestUsernames.update { it - requesterUid }

                Log.d("UserFriendManager", "✅ Declined request from $requesterUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to decline request: $error")
            }
        }
    }

    fun deleteFriend(friendUid: String) {
        val currentUser = getUser() ?: return

        userRepository.deleteFriend(currentUser.uid, friendUid) { success, error ->
            if (success) {
                updateUser(currentUser.copy(
                    friendList = currentUser.friendList - friendUid
                ))

                viewModelScope.launch {
                    userRepository.removeLocalFriend(friendUid)
                }

                Log.d("UserFriendManager", "✅ Deleted friend $friendUid")
            } else {
                Log.e("UserFriendManager", "❌ Failed to delete friend: $error")
            }
        }
    }

    fun fetchUserInventory() {
        val currentUser = getUser() ?: return

        viewModelScope.launch {
            val inventory = userRepository.fetchInventory(currentUser.uid) ?: return@launch

            val updatedUser = currentUser.copy(
                gold = inventory.gold,
                runes_rare = inventory.runes_rare,
                runes_super_rare = inventory.runes_super_rare,
                runes_mega_rare = inventory.runes_mega_rare
            )

            updateUser(updatedUser)
        }
    }

    // UserManager.kt
    fun setFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = getUser() ?: return
        viewModelScope.launch {
            if (fav.content.isBlank() || fav.senderId.isBlank()) {
                onFailure("Missing local content or sender.")
                return@launch
            }

            userRepository.setFavouriteMessage(
                fav = fav,
                onSuccess = {
                    userRepository.getPublicUser(currentUser.uid)
                        .addOnSuccessListener { doc ->
                            val public = doc.toObject(PublicUser::class.java)
                            val updated = currentUser.copy(favouriteMessage = public?.favouriteMessage)
                            updateUser(updated)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onFailure(e.message ?: "Updated, but failed to refresh favourite")
                        }
                },
                onFailure = { e -> onFailure(e.message ?: "Failed to set favourite") }
            )
        }
    }

    /** 🗑️ Clear the favourite message */
    fun clearFavouriteMessage(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = getUser() ?: return
        viewModelScope.launch {
            userRepository.clearFavouriteMessage(
                onSuccess = {
                    // We can update locally without a read
                    val updated = currentUser.copy(favouriteMessage = null)
                    updateUser(updated)
                    onSuccess()
                },
                onFailure = { e ->
                    onFailure(e.message ?: "Failed to clear favourite")
                }
            )
        }
    }

    fun toggleFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = getUser() ?: return
        val isSame = currentUser.favouriteMessage?.messageId == fav.id
        if (isSame) {
            clearFavouriteMessage(onSuccess, onFailure)
        } else {
            setFavouriteMessage(fav, onSuccess, onFailure)
        }
    }
    // ... Add more methods here in the same structure ...


}
