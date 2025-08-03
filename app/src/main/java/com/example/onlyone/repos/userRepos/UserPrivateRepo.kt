package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.data.UserPrivate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrivateRepo @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "❌ Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUid = auth.currentUser?.uid

            if (currentUid != null && token != null) {
                db.collection("users_private")
                    .document(currentUid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "✅ Token saved to Firestore: $token")
                    }
                    .addOnFailureListener {
                        Log.e("FCM", "❌ Failed to save token", it)
                    }
            }
        }
    }

    fun updateNotificationSetting(
        uid: String,
        key: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val path = "notifications.$key"
        db.collection("users_private").document(uid)
            .update(path, enabled)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    suspend fun getPrivateUserSuspend(uid: String): UserPrivate {
        val doc = db.collection("users_private").document(uid).get().await()
        val data = doc.data ?: throw Exception("Missing users_private/$uid")
        return UserPrivate(
            uid = uid,
            email = data["email"] as? String ?: "",
            notifications = data["notifications"] as? Map<String, Boolean> ?: mapOf(),
            blockList = data["blockList"] as? List<String> ?: emptyList(),
            friendList = data["friendList"] as? List<String> ?: emptyList(),
            incomingFriendRequests = data["incomingFriendRequests"] as? List<String> ?: emptyList(),
            outgoingFriendRequests = data["outgoingFriendRequests"] as? List<String> ?: emptyList(),
            reportCount = (data["reportCount"] as? Number)?.toInt() ?: 0
        )
    }

}
