package com.onlyone.app.viewModels.userViewModel

import android.util.Log
import com.onlyone.app.data.UserComposite
import com.onlyone.app.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ShopManager(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val getUser: () -> UserComposite?,
    private val updateUser: (UserComposite) -> Unit
) {

    fun buyAvatar(
        avatarId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = getUser() ?: return

        viewModelScope.launch {
            val updatedInventory = userRepository.buyAvatar(avatarId)

            if (updatedInventory != null) {
                val updatedUser = currentUser.copy(
                    gold = updatedInventory.gold,
                    runes_rare = updatedInventory.runes_rare,
                    runes_super_rare = updatedInventory.runes_super_rare,
                    runes_mega_rare = updatedInventory.runes_mega_rare,
                    ownedAvatars = updatedInventory.ownedAvatars
                )

                updateUser(updatedUser)
                onSuccess()
            } else {
                onFailure("Could not complete purchase.")
                Log.e("ShopManager", "❌ buyAvatar failed for avatarId=$avatarId")
            }
        }
    }

    fun buyMood(
        moodId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = getUser() ?: return
        viewModelScope.launch {
            val updatedInventory = userRepository.buyMood(moodId)
            if (updatedInventory != null) {
                val updatedUser = currentUser.copy(
                    gold = updatedInventory.gold,
                    runes_rare = updatedInventory.runes_rare,
                    runes_super_rare = updatedInventory.runes_super_rare,
                    runes_mega_rare = updatedInventory.runes_mega_rare,
                    ownedAvatars = updatedInventory.ownedAvatars,
                    // ⭐ reflect moods
                    ownedMoods = updatedInventory.ownedMoods
                )
                updateUser(updatedUser)
                onSuccess()
            } else {
                onFailure("Could not complete purchase.")
                Log.e("ShopManager", "❌ buyMood failed for moodId=$moodId")
            }
        }
    }

    fun buyTheme(
        themeId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = getUser() ?: return

        viewModelScope.launch {
            val updatedInventory = userRepository.buyTheme(themeId)

            if (updatedInventory != null) {
                val updatedUser = currentUser.copy(
                    gold = updatedInventory.gold,
                    runes_rare = updatedInventory.runes_rare,
                    runes_super_rare = updatedInventory.runes_super_rare,
                    runes_mega_rare = updatedInventory.runes_mega_rare,
                    ownedAvatars = updatedInventory.ownedAvatars,
                    ownedMoods = updatedInventory.ownedMoods,
                    ownedThemes = updatedInventory.ownedThemes   // ← requires this field
                )
                updateUser(updatedUser)
                onSuccess()
            } else {
                onFailure("Could not complete purchase.")
                Log.e("ShopManager", "❌ buyTheme failed for themeId=$themeId")
            }
        }
    }

}
