package com.example.onlyone.viewModels.userViewModel

import com.example.onlyone.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope

class UpgradeManager(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val loadUser: () -> Unit
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
            onSuccess = { _, _ ->
                loadUser() // refresh after upgrade
                onSuccess()
            },
            onFailure = onFailure
        )
    }
}

