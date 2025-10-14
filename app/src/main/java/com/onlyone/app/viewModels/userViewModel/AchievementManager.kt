package com.onlyone.app.viewModels.userViewModel

import android.util.Log
import com.onlyone.app.data.UserComposite
import com.onlyone.app.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AchievementManager(
    private val userRepository: UserRepository,
    private val scope: CoroutineScope,
    private val getUser: () -> UserComposite?
) {
    private var cachedDefinitions: List<Map<String, Any>>? = null
    private var cachedUserAchievements: Map<String, Any>? = null
     var cachedUserStats: Map<String, Any>? = null // 🆕

    fun getCachedDefinitions(): List<Map<String, Any>>? = cachedDefinitions
    fun getCachedUserAchievements(): Map<String, Any>? = cachedUserAchievements

    fun setUserStats(stats: Map<String, Any>) {
        cachedUserStats = stats
    }

    fun loadUserAchievementsWithStats(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            val achievements = userRepository.getUserAchievements()
            val stats = userRepository.fetchUserStats()

            if (achievements == null) {
                Log.e("Achievements", "❌ getUserAchievements() returned null")
            } else {
                Log.d("Achievements", "✅ getUserAchievements() returned keys: ${achievements.keys}")
            }

            if (stats == null) {
                Log.e("Achievements", "❌ fetchUserStats() returned null")
            } else {
                Log.d("Achievements", "✅ fetchUserStats() = $stats")
            }

            if (achievements != null && stats != null) {
                cachedUserAchievements = achievements
                cachedUserStats = stats
                onSuccess()
            } else {
                onFailure("Failed to load achievements or stats")
            }
        }
    }


    fun fetchDefinitions(
        forceRefresh: Boolean = false,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!forceRefresh && cachedDefinitions != null) {
            onSuccess(cachedDefinitions!!)
            return
        }

        scope.launch {
            val defs = userRepository.fetchAchievementDefinitions()
            if (defs.isNotEmpty()) {
                Log.d("Achievement", "✅ fetchAchievementDefinitions")
                cachedDefinitions = defs
                onSuccess(defs)
            } else {
                onFailure("No definitions found")
            }
        }
    }

    fun groupAchievementsByType(
        definitions: List<Map<String, Any>>,
        achievedMap: Map<String, Any>
    ): Map<String, List<AchievementWithProgress>> {
        val statsMap = cachedUserStats ?: emptyMap()

        return definitions.mapNotNull { def ->
            val id = def["id"] as? String ?: return@mapNotNull null
            val name = def["name"] as? String ?: "Unnamed"
            val description = def["description"] as? String ?: ""
            val threshold = (def["threshold"] as? Number)?.toInt() ?: 0
            val type = def["type"] as? String ?: return@mapNotNull null
            val points = (def["points"] as? Number)?.toInt() ?: 0
            val rarity = def["rarity"] as? String ?: "common"
            val icon = def["icon"] as? String ?: "✨"

            val isCompleted = achievedMap.containsKey(id)
            val currentProgress = if (isCompleted) {
                threshold
            } else {
                (statsMap[type] as? Number)?.toInt() ?: 0
            }
            Log.d("Achievements", "→ Checking: id=$id, type=$type, threshold=$threshold")

            if (isCompleted) {
                Log.d("Achievements", "✔️ Already completed: $id")
            } else {
                Log.d("Achievements", "📊 Stat progress for $type = ${(statsMap[type] as? Number)?.toInt()}")
            }

            AchievementWithProgress(
                id = id,
                name = name,
                description = description,
                threshold = threshold,
                progress = currentProgress.coerceAtMost(threshold),
                completed = isCompleted,
                icon = icon,
                points = points,
                rarity = rarity,
                type = type
            )
        }.groupBy { it.type }
    }

    data class AchievementWithProgress(
        val id: String,
        val name: String,
        val description: String,
        val threshold: Int,
        val progress: Int,
        val completed: Boolean,
        val icon: String,
        val points: Int,
        val rarity: String,
        val type: String
    )
}

