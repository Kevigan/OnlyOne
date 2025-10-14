package com.onlyone.app.repos

import android.util.Log
import com.onlyone.app.data.UserInventory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserInventoryRepo @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun fetchInventory(uid: String): UserInventory? {
        return try {
            val doc = db.collection("users_inventory").document(uid).get().await()
            val data = doc.data ?: return null
            UserInventory(
                gold = (data["gold"] as? Number)?.toInt() ?: 0,
                runes_rare = (data["runes_rare"] as? Number)?.toInt() ?: 0,
                runes_super_rare = (data["runes_super_rare"] as? Number)?.toInt() ?: 0,
                runes_mega_rare = (data["runes_mega_rare"] as? Number)?.toInt() ?: 0,
                ownedAvatars = (data["ownedAvatars"] as? List<*>)
                    ?.filterIsInstance<Number>()
                    ?.map { it.toInt() }
                    ?: emptyList(),
                ownedMoods = (data["ownedMoods"] as? List<*>)              // ⭐ NEW
                    ?.filterIsInstance<Number>()
                    ?.map { it.toInt() }
                    ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to fetch inventory", e)
            null
        }
    }

    suspend fun buyAvatar(avatarId: Int): UserInventory? {
        return try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("buyAvatar")
                .call(mapOf("avatarId" to avatarId))
                .await()

            val uid = Firebase.auth.currentUser?.uid ?: return null
            fetchInventory(uid)
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to buy avatar", e)
            null
        }
    }

    // ⭐ NEW: mirror of buyAvatar for moods
    suspend fun buyMood(moodId: Int): UserInventory? {
        return try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("buyMood")
                .call(mapOf("moodId" to moodId))
                .await()

            val uid = Firebase.auth.currentUser?.uid ?: return null
            fetchInventory(uid)
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to buy mood", e)
            null
        }
    }

    suspend fun buyTheme(themeId: Int): UserInventory? {
        return try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("buyTheme")
                .call(mapOf("themeId" to themeId))
                .await()

            val uid = Firebase.auth.currentUser?.uid ?: return null
            fetchInventory(uid)
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to buy theme", e)
            null
        }
    }
}

