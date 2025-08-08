package com.example.onlyone.repos.userRepos

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAchievementRepo @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getUserAchievements(): Map<String, Any>? {
        return try {
            val result = Firebase.functions("europe-west3")
                .getHttpsCallable("getUserAchievements")
                .call()
                .await()

            result.data as? Map<String, Any>
        } catch (e: Exception) {
            Log.e("UserAchievementRepo", "❌ Failed to get user achievements", e)
            null
        }
    }

    suspend fun fetchAchievementDefinitions(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("achievements_definitions").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserAchievementRepo", "❌ Failed to fetch definitions", e)
            emptyList()
        }
    }

    suspend fun fetchUserStats(): Map<String, Any>? {
        return try {
            val uid = Firebase.auth.currentUser?.uid ?: run {
                Log.e("UserRepo", "❌ No current user signed in")
                return null
            }
            Log.d("UserRepo", "🔍 Fetching stats for UID: $uid")

            val doc = db.collection("users_stats").document(uid).get().await()

            if (!doc.exists()) {
                Log.e("UserRepo", "❌ Document does not exist")
                return null
            }

            Log.d("UserRepo", "✅ Document fetched: ${doc.data}")
            doc.data
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Exception while fetching stats", e)
            null
        }
    }

}
