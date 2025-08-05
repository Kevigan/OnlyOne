package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.data.UserUpgrades
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUpgradeRepo @Inject constructor(private val db: FirebaseFirestore) {

    fun upgradeFeature(
        feature: String,
        levels: Int,
        onSuccess: (newValue: Int, remainingGold: Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf("feature" to feature, "amount" to levels)

        Firebase.functions("europe-west3")
            .getHttpsCallable("upgradeFeature")
            .call(data)
            .addOnSuccessListener { result ->
                val dataMap = result.data as? Map<*, *> ?: return@addOnSuccessListener
                val newValue = (dataMap["newValue"] as? Number)?.toInt() ?: 0
                val remainingGold = (dataMap["remaining"] as? Map<*, *>)?.get("gold")?.let { it as? Number }?.toInt() ?: 0

                Log.d("UpgradeRepo", "✅ Upgraded $feature to $newValue (remaining gold: $remainingGold)")
                onSuccess(newValue, remainingGold)
            }
            .addOnFailureListener { error ->
                Log.e("UpgradeRepo", "❌ Upgrade failed for $feature", error)
                onFailure(error)
            }
    }

    suspend fun fetchUpgrades(uid: String): UserUpgrades {
        val doc = db.collection("users_upgrades").document(uid).get().await()
        val data = doc.data ?: return UserUpgrades()
        return UserUpgrades(
            maxMessageLength = (data["maxMessageLength"] as? Number)?.toInt() ?: 25,
            maxMoments = (data["maxMoments"] as? Number)?.toInt() ?: 75,
            maxSwipes = (data["maxSwipes"] as? Number)?.toInt() ?: 50,
            maxAdsPerDay = (data["maxAdsPerDay"] as? Number)?.toInt() ?: 3
        )
    }
}
