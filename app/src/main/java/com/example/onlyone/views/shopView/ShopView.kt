package com.example.onlyone.views.shopView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ShopView(userViewModel: UserViewModel) {
    val moodImages = listOf(
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
    )
    val user by userViewModel.user.observeAsState()
    var showMessageLengthDialog by remember { mutableStateOf(false) }
    var showSwipesDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 🔷 Shop Title
                    Text(
                        text = stringResource(R.string.shop_title),
                        style = MaterialTheme.typography.h5,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 🔷 Coins + Value
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.common_gold),
                            style = MaterialTheme.typography.h5,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp, bottom = 16.dp)
                        )

                        Text(
                            text = (user?.gold ?: 0).toString(),
                            style = MaterialTheme.typography.h5,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                // Avatars
                AvatarsSection(
                    user = user,
                    onBuyAvatar = { avatarId ->
                        userViewModel.buyAvatar(
                            avatarId = avatarId,
                            onSuccess = { /* show toast using common_buy/common_owned if needed */ },
                            onFailure = { /* show error using common_purchase_failed */ }
                        )
                    },
                    onSelectAvatar = { avatarId ->
                        userViewModel.updatePublicProfile(
                            updates = mapOf("avatarId" to avatarId),
                            onSuccess = {},
                            onFailure = {}
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Moods (title inside your section can use common_moods if you display one)
                MoodsSection(moodImages)

                if (user != null) {
                    // Build current values with plurals
                    val currentChars = pluralStringResource(
                        R.plurals.common_chars,
                        user!!.maxMessageLength,
                        user!!.maxMessageLength
                    )
                    val currentSwipesPerDay = pluralStringResource(
                        R.plurals.common_swipes_per_day,
                        user!!.maxSwipes,
                        user!!.maxSwipes
                    )

                    val upgrades = listOf(
                        Triple(
                            stringResource(R.string.shop_message_length_label),
                            stringResource(R.string.common_current, currentChars)
                        ) { showMessageLengthDialog = true },
                        Triple(
                            stringResource(R.string.shop_swipes_limit_label),
                            stringResource(R.string.common_current, currentSwipesPerDay)
                        ) { showSwipesDialog = true }
                    )

                    upgrades.forEach { (label, valueText, onClick) ->
                        Spacer(modifier = Modifier.height(8.dp))
                        UpgradeRow(
                            label = label,
                            currentValueText = valueText,
                            onUpgradeClick = onClick
                        )
                    }
                }
            }
        }

        // 🔧 Dialogs
        if (showMessageLengthDialog && user != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_message_length_title),
                feature = "maxMessageLength",
                currentValue = user!!.maxMessageLength, // ✅ bugfix: was maxSwipes
                userGold = user!!.gold,
                userRunesRare = user!!.runes_rare,
                userRunesSuperRare = user!!.runes_super_rare,
                userRunesMegaRare = user!!.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxMessageLength",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showMessageLengthDialog = false }
            )
        }

        if (showSwipesDialog && user != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_swipes_title),
                feature = "maxSwipes",
                currentValue = user!!.maxSwipes,
                userGold = user!!.gold,
                userRunesRare = user!!.runes_rare,
                userRunesSuperRare = user!!.runes_super_rare,
                userRunesMegaRare = user!!.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxSwipes",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showSwipesDialog = false }
            )
        }
    }
}
