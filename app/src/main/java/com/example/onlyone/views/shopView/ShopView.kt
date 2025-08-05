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
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ShopView(userViewModel: UserViewModel) {
    val moodImages = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
    )
    val user by userViewModel.user.observeAsState()
    var showMessageLengthDialog by remember { mutableStateOf(false) }
    var showSwipesDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        )
        {
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
                        text = "Shop",
                        style = MaterialTheme.typography.h5,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 🔷 Coins + Value
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gold",
                            style = MaterialTheme.typography.h5,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp, bottom = 16.dp)
                        )

                        Text(
                            text = user?.gold.toString(),
                            style = MaterialTheme.typography.h5,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                AvatarsSection(
                    user = user,
                    onBuyAvatar = { avatarId ->
                        userViewModel.buyAvatar(
                            avatarId = avatarId,
                            onSuccess = { /* toast */ },
                            onFailure = { /* error */ }
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

                MoodsSection(moodImages)

                if (user != null) {
                    val upgrades = listOf(
                        Triple(
                            "Message Length Limit",
                            "Current: ${user!!.maxMessageLength} chars"
                        ) { showMessageLengthDialog = true },
                        Triple(
                            "Swipes Limit",
                            "Current: ${user!!.maxSwipes} swipes/day"
                        ) { showSwipesDialog = true }

                        // You can add more here in future
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

        if (showMessageLengthDialog && user != null) {//maxMessageLength  "Upgrade Message Length" showMessageLengthDialog
            UpgradeDialog(
                title = "Upgrade Message Length",
                feature = "maxMessageLength",
                currentValue = user!!.maxSwipes,
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
                title = "Upgrade Swipes Limit",
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