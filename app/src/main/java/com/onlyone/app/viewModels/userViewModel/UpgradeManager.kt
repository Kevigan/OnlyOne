package com.onlyone.app.viewModels.userViewModel

import com.onlyone.app.data.UserComposite
import com.onlyone.app.repos.userRepos.UserRepository

class UpgradeManager(
    private val userRepository: UserRepository,
    private val getUser: () -> UserComposite?,
    private val updateUser: (UserComposite) -> Unit
) {
    fun upgradeFeature(
        feature: String,
        levels: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.upgradeFeature(
            feature = feature,
            levels = levels,
            onSuccess = { newAbsolute, remaining ->
                // Optimistic UI: apply the absolute returned by server
                val current = getUser()
                if (current != null) {
                    val updated = when (feature) {
                        "maxMessageLength" -> current.copy(maxMessageLength = newAbsolute)
                        "maxSwipes"        -> current.copy(maxSwipes = newAbsolute)
                        "maxMoodLength"    -> current.copy(maxMoodLength = newAbsolute)
                        "maxMoments"       -> current.copy(maxMoments = newAbsolute)
                        "maxAdsPerDay"     -> current.copy(maxAdsPerDay = newAbsolute)
                        else               -> current
                    }.copy(
                        gold = remaining["gold"] ?: current.gold,
                        runes_rare = remaining["runes_rare"] ?: current.runes_rare,
                        runes_super_rare = remaining["runes_super_rare"] ?: current.runes_super_rare,
                        runes_mega_rare = remaining["runes_mega_rare"] ?: current.runes_mega_rare
                    )
                    updateUser(updated)
                }
                onSuccess()
            },
            onFailure = onFailure
        )
    }
}
