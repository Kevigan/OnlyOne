package com.onlyone.app.repos.userRepos

import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

data class FriendRequestResult(
    val toUid: String,
    val toUsername: String? // may be null if CF didn't return
)

data class AcceptFriendResult(
    val requesterUid: String,
    val requesterUsername: String?
)

@Singleton
class UserFriendRepo @Inject constructor() {
    fun sendFriendRequest(
        fromUid: String,
        toUid: String,
        onComplete: (Result<FriendRequestResult>) -> Unit
    ) {
        val data = mapOf("toUid" to toUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("sendFriendRequest")
            .call(data)
            .addOnSuccessListener { res ->
                val m = res.data as? Map<*, *>
                val uname = m?.get("toUsername") as? String
                onComplete(Result.success(FriendRequestResult(toUid, uname)))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    fun acceptFriendRequest(
        currentUid: String,
        requesterUid: String,
        onComplete: (Result<AcceptFriendResult>) -> Unit
    ) {
        val data = mapOf("requesterUid" to requesterUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("acceptFriendRequest")
            .call(data)
            .addOnSuccessListener { res ->
                val m = res.data as? Map<*, *>
                val uname = m?.get("requesterUsername") as? String
                onComplete(Result.success(AcceptFriendResult(requesterUid, uname)))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    fun cancelOutgoingFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("toUid" to toUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("cancelOutgoingFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendCancel", "✅ Outgoing request canceled via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendCancel", "❌ Failed to cancel outgoing request: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun declineFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("requesterUid" to requesterUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("declineFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendDecline", "✅ Declined request via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendDecline", "❌ Failed to decline request: ${e.message}", e)
                onComplete(false, e.message)
            }
    }
    fun deleteFriend(currentUid: String, targetUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("targetUid" to targetUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("deleteFriend")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendDelete", "✅ Friend deleted via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendDelete", "❌ Failed to delete friend: ${e.message}", e)
                onComplete(false, e.message)
            }
    }
    fun blockAndUnfriendUser(currentUid: String, blockedUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("blockedUid" to blockedUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("blockAndUnfriendUser")
            .call(data)
            .addOnSuccessListener { result ->
                Log.d("BlockUser", "✅ Blocked (and unfriended if needed) via cloud")

                // optional: check return value if you want to inspect result.data
                val response = result.data as? Map<*, *>
                val unfriended = response?.get("unfriended") as? Boolean ?: false
                Log.d("BlockUser", "→ Was unfriended: $unfriended")

                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("BlockUser", "❌ Failed to block user: ${e.message}", e)
                onComplete(false, e.message)
            }
    }
    fun unblockUser(targetUid: String, onComplete: (Boolean) -> Unit) {
        val data = mapOf("targetUid" to targetUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("unblockUser")
            .call(data)
            .addOnSuccessListener {
                Log.d("UnblockUser", "✅ Unblocked user $targetUid")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("UnblockUser", "❌ Failed to unblock user: ${e.message}", e)
                onComplete(false)
            }
    }
    fun findUserByEmail(email: String, onResult: (String?) -> Unit) {

        val data = mapOf("email" to email)

        Firebase.functions("europe-west3")
            .getHttpsCallable("getUidByEmail")
            .call(data)
            .addOnSuccessListener { result ->
                val uid = (result.data as? Map<*, *>)?.get("uid") as? String
                Log.d("EmailTracker", "Cloud function success: $uid")
                onResult(uid)
            }
            .addOnFailureListener { error ->
                Log.e("EmailTracker", "Cloud function failure: ${error.message}", error)
                onResult(null)
            }
    }
}
