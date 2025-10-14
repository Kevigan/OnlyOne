package com.onlyone.app.views.shopView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.theme.ThemeViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeId

@Composable
fun ShopView(userViewModel: UserViewModel, themeViewModel: ThemeViewModel, theme: ThemeTokens) {
    val user by userViewModel.user.observeAsState()
    var showMessageLengthDialog by remember { mutableStateOf(false) }
    var showSwipesDialog by remember { mutableStateOf(false) }
    var showMoodLengthDialog by remember { mutableStateOf(false) }
    var loadingAvatarId by remember { mutableStateOf<Int?>(null) }
    var loadingMoodId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val currentThemeId by themeViewModel.id.collectAsState()
    var loadingThemeId by remember { mutableStateOf<ThemeId?>(null) }
    val scrollState = rememberScrollState()
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
                .verticalScroll(scrollState)
        ) {
            // HEADER CARD (title + gold + help icon)
            CustomColorOverlay(
                modifier = Modifier.fillMaxWidth(),
                paddingBox1 = PaddingValues(5.dp),
                paddingBox2 = PaddingValues(5.dp),
                gradientColor1 = Color(0xFF001F54).copy(alpha = 0.95f),
                gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
                borderWidth = 1.dp,
                shape = RoundedCornerShape(12.dp),
                theme = theme,
                onDismiss = {}
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // LEFT: title + help icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.shop_title),
                            style = MaterialTheme.typography.h5,
                            color = theme.textColor
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.shop_info_cd),
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }

                    // RIGHT: gold
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.common_gold_label),
                            style = MaterialTheme.typography.h5,
                            color = theme.textColor,
                            modifier = Modifier.padding(end = 8.dp, bottom = 16.dp)
                        )
                        Text(
                            text = (user?.gold ?: 0).toString(),
                            style = MaterialTheme.typography.h5,
                            color = theme.textColor,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            } // ← close header overlay

            Spacer(modifier = Modifier.height(16.dp))

            // AVATARS
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
                theme = theme,
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

            // MOODS
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
                theme = theme,
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

            Spacer(modifier = Modifier.height(16.dp))

            // THEMES
            ThemesSection(
                currentThemeId = currentThemeId,
                ownedThemeIds = user?.ownedThemes ?: emptyList(),
                loadingThemeId = loadingThemeId,
                onSelectTheme = { id ->
                    loadingThemeId = id
                    themeViewModel.select(id)  // persist + apply
                    loadingThemeId = null
                    Toast.makeText(
                        context,
                        context.getString(R.string.shop_theme_select_success),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                theme = theme,
                onBuyTheme = { id ->
                    loadingThemeId = id
                    userViewModel.buyTheme(
                        themeId = id.ordinal,
                        onSuccess = {
                            // Auto-apply right after purchase
                            themeViewModel.select(id)
                            loadingThemeId = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.shop_theme_buy_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { reason ->
                            loadingThemeId = null
                            Toast.makeText(
                                context,
                                reason.ifBlank { context.getString(R.string.shop_theme_buy_failed) },
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )

            // UPGRADES
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
                        onUpgradeClick = onClick,
                        theme = theme,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // breathing room
            Spacer(modifier = Modifier.height(24.dp))
        }

        // UPGRADE DIALOGS
        val u = user
        if (showMessageLengthDialog && u != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_message_length_title),
                feature = "maxMessageLength",
                currentValue = u.maxMessageLength,
                userGold = u.gold,
                userRunesRare = u.runes_rare,
                userRunesSuperRare = u.runes_super_rare,
                userRunesMegaRare = u.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxMessageLength",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showMessageLengthDialog = false },
                theme = theme
            )
        }

        if (showSwipesDialog && u != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_swipes_title),
                feature = "maxSwipes",
                currentValue = u.maxSwipes,
                userGold = u.gold,
                userRunesRare = u.runes_rare,
                userRunesSuperRare = u.runes_super_rare,
                userRunesMegaRare = u.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxSwipes",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showSwipesDialog = false },
                theme = theme
            )
        }

        if (showMoodLengthDialog && u != null) {
            UpgradeDialog(
                title = stringResource(R.string.shop_upgrade_mood_length_title),
                feature = "maxMoodLength",
                currentValue = u.maxMoodLength,
                userGold = u.gold,
                userRunesRare = u.runes_rare,
                userRunesSuperRare = u.runes_super_rare,
                userRunesMegaRare = u.runes_mega_rare,
                onConfirm = { callback ->
                    userViewModel.upgradeFeature(
                        feature = "maxMoodLength",
                        levels = 1,
                        onSuccess = { callback(true) },
                        onFailure = { callback(false) }
                    )
                },
                onDismiss = { showMoodLengthDialog = false },
                theme = theme,
            )
        }

        // INFO DIALOG (reusable)
        if (showInfoDialog) {
            InfoDialog(
                title = stringResource(R.string.shop_info_title),
                message = stringResource(R.string.shop_info_body),
                theme = theme,
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}

