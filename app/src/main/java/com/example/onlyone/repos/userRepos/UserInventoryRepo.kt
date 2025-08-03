package com.example.onlyone.repos

import android.util.Log
import com.example.onlyone.data.UserInventory
import com.google.firebase.firestore.FirebaseFirestore
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
                runes_mega_rare = (data["runes_mega_rare"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to fetch inventory", e)
            null
        }
    }

    suspend fun updateGold(uid: String, newAmount: Int): Boolean {
        return try {
            db.collection("users_inventory").document(uid)
                .update("gold", newAmount)
                .await()
            true
        } catch (e: Exception) {
            Log.e("InventoryRepo", "❌ Failed to update gold", e)
            false
        }
    }

    // Optional: Add similar update functions for each rune type if needed
}
