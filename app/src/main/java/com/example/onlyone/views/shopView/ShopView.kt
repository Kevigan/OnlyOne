package com.example.onlyone.views.shopView

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ShopView(userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()
    var showMessageLengthDialog by remember { mutableStateOf(false) }
    var showSwipesDialog by remember { mutableStateOf(false) }
    var showMoodLengthDialog by remember { mutableStateOf(false) }
    var loadingAvatarId by remember { mutableStateOf<Int?>(null) }
    var loadingMoodId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
                .verticalScroll(scrollState) // ← enable scrolling
        ) {
            // Header card
            CustomColorOverlay(
                modifier = Modifier.fillMaxWidth(),
                paddingBox1 = PaddingValues(5.dp),
                paddingBox2 = PaddingValues(5.dp),
                gradientColor1 = Color(0xFF001F54).copy(alpha = 0.95f),
                gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
                borderWidth = 1.dp,
                shape = RoundedCornerShape(12.dp),
                onDismiss = {}
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.shop_title),
                        style = MaterialTheme.typography.h5,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.common_gold_label),
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
            }

            // Avatars
            AvatarsSection(
                user = user,
                loadingAvatarId = loadingAvatarId,
                onBuyAvatar = { avatarId ->
                    loadingAvatarId = avatarId
                    userViewModel.buyAvatar(
                        avatarId = avatarId,
                        onSuccess = {
                            loadingAvatarId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_avatar_buy_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            loadingAvatarId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_avatar_buy_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                onSelectAvatar = { avatarId ->
                    loadingAvatarId = avatarId
                    userViewModel.updatePublicProfile(
                        updates = mapOf("avatarId" to avatarId),
                        onSuccess = {
                            loadingAvatarId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_avatar_select_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            loadingAvatarId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_avatar_select_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Moods
            MoodsSection(
                user = user,
                loadingMoodId = loadingMoodId,
                onBuyMood = { moodId ->
                    loadingMoodId = moodId
                    userViewModel.buyMood(
                        moodId = moodId,
                        onSuccess = {
                            loadingMoodId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_mood_buy_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            loadingMoodId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_mood_buy_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },
                onSelectMood = { moodId ->
                    loadingMoodId = moodId
                    userViewModel.updatePublicProfile(
                        updates = mapOf("moodId" to moodId),
                        onSuccess = {
                            loadingMoodId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_mood_select_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = {
                            loadingMoodId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_mood_select_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )

            // Upgrades (scrollable now)
            if (user != null) {
                val currentChars = pluralStringResource(
                    R.plurals.common_chars, user!!.maxMessageLength, user!!.maxMessageLength
                )
                val currentSwipesPerDay = pluralStringResource(
                    R.plurals.common_swipes_per_day, user!!.maxSwipes, user!!.maxSwipes
                )
                val currentMoodChars = pluralStringResource(
                    R.plurals.common_chars, user!!.maxMoodLength, user!!.maxMoodLength
                )

                val upgrades = listOf(
                    Triple(
                        stringResource(R.string.shop_message_length_label),
                        stringResource(R.string.common_current, currentChars)
                    ) { showMessageLengthDialog = true },
                    Triple(
                        stringResource(R.string.shop_swipes_limit_label),
                        stringResource(R.string.common_current, currentSwipesPerDay)
                    ) { showSwipesDialog = true },
                    Triple(
                        stringResource(R.string.shop_mood_length_label),
                        stringResource(R.string.common_current, currentMoodChars)
                    ) { showMoodLengthDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))
                upgrades.forEach { (label, valueText, onClick) ->
                    UpgradeRow(
                        label = label,
                        currentValueText = valueText,
                        onUpgradeClick = onClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // give a little breathing room so last item isn’t obscured
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dialogs (unchanged)
        if (showMessageLengthDialog && user != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_message_length_title),
                feature = "maxMessageLength",
                currentValue = user!!.maxMessageLength,
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

        if (showMoodLengthDialog && user != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_mood_length_title),
                feature = "maxMoodLength",
                currentValue = user!!.maxMoodLength,
                userGold = user!!.gold,
                userRunesRare = user!!.runes_rare,
                userRunesSuperRare = user!!.runes_super_rare,
                userRunesMegaRare = user!!.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxMoodLength",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showMoodLengthDialog = false }
            )
        }
    }
}
