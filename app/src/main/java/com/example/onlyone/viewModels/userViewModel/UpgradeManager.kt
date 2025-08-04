package com.example.onlyone.viewModels.userViewModel

import com.example.onlyone.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope

class UpgradeManager(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val loadUser: () -> Unit
) {

    fun upgradeMaxMessageLength(
        levels: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userRepository.upgradeMaxMessageLength(
            levels = levels,
            onSuccess = { _, _ ->
                loadUser() // ✅ Clean reuse
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    // Future: add other upgrades
}
