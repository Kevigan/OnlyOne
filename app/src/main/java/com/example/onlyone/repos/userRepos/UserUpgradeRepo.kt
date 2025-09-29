package com.example.onlyone.repos.userRepos

import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUpgradeRepo @Inject constructor() {

    fun upgradeFeature(
        feature: String,
        levels: Int,
        onSuccess: (newAbsolute: Int, remaining: Map<String, Int>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf("feature" to feature, "amount" to levels)

        Firebase.functions("europe-west3")
            .getHttpsCallable("upgradeFeature")
            .call(data)
            .addOnSuccessListener { result ->
                val dataMap = result.data as? Map<*, *> ?: return@addOnSuccessListener

                val newAbsolute = (dataMap["newAbsolute"] as? Number)?.toInt()
                    ?: (dataMap["newValue"] as? Number)?.toInt()
                    ?: 0

                val remainingRaw = (dataMap["remaining"] as? Map<*, *>) ?: emptyMap<Any, Any>()
                val remaining = remainingRaw.mapNotNull { (k, v) ->
                    (k as? String)?.let { it to ((v as? Number)?.toInt() ?: 0) }
                }.toMap()

                Log.d("UpgradeRepo", "✅ Upgraded $feature → abs=$newAbsolute, remaining=$remaining")
                onSuccess(newAbsolute, remaining)
            }
            .addOnFailureListener { error ->
                Log.e("UpgradeRepo", "❌ Upgrade failed for $feature", error)
                onFailure(error)
            }
    }
}
