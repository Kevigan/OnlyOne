package com.onlyone.app.repos

import android.util.Log
import com.onlyone.app.data.UserEngagementStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserEngagementRepo @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun fetchEngagementStatus(uid: String, onComplete: (UserEngagementStatus?) -> Unit) {
        db.collection("engagement_status").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                if (data != null) {
                    val status = UserEngagementStatus(
                        uid = uid,
                        swipesUsed = (data["swipesUsed"] as? Number)?.toInt() ?: 0,
                        momentsAvailable = (data["momentsAvailable"] as? Number)?.toInt() ?: 75,
                        adsWatchedToday = (data["adsWatchedToday"] as? Number)?.toInt() ?: 0,
                        lastRefill = data["lastRefill"] as? Timestamp
                    )
                    onComplete(status)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                Log.e("EngagementRepo", "❌ Failed to load engagement status", it)
                onComplete(null)
            }
    }

    suspend fun incrementSwipeCount(): Boolean {
        return try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("incrementSwipeCount")
                .call()
                .await()

            Log.d("EngagementRepo", "✅ Cloud swipe increment success")
            true
        } catch (e: Exception) {
            Log.e("EngagementRepo", "❌ Swipe increment failed: ${e.message}", e)
            false
        }
    }

    suspend fun fetchEngagementStatusSuspend(uid: String): UserEngagementStatus {
        val doc = db.collection("engagement_status").document(uid).get().await()
        val data = doc.data ?: throw Exception("Missing engagement_status/$uid")
        return UserEngagementStatus(
            uid = uid,
            swipesUsed = (data["swipesUsed"] as? Number)?.toInt() ?: 0,
            momentsAvailable = (data["momentsAvailable"] as? Number)?.toInt() ?: 75,
            adsWatchedToday = (data["adsWatchedToday"] as? Number)?.toInt() ?: 0,
            lastRefill = data["lastRefill"] as? Timestamp
        )
    }

}
