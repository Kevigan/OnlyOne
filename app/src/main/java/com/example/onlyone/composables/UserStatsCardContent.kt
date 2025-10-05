package com.example.onlyone.composables

import com.example.onlyone.data.UserComposite
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.ui.avatar.AvatarPickerDialog

@Composable
fun UserStatsCardContent(
    theme: ThemeTokens,
    user: UserComposite?,                 // nullable for loading
    onMoodSubmit: (String) -> Unit,
    onMoodIconSelected: (Int) -> Unit,
    onAvatarSelected: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    onFeedbackClick: (() -> Unit)? = null,
    navController: NavController,         // ✅ NEW: for Achievements navigation
    modifier: Modifier = Modifier
) {
    val avatarResId = remember(user?.avatarId) { mapAvatarIdToDrawable(user?.avatarId) }
    val username = user?.username.orEmpty()
    val gold = user?.gold ?: 0
    val ownedAvatarIds = user?.ownedAvatars ?: emptyList()

    val dividerColor = Color.White.copy(alpha = 0.15f)
    var showAvatarDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // ===== Top bar: Username + Gold/Achievements (left) | Feedback + Logout (right) =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 💰 Gold + 🏅 Achievements row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.stats_gold, gold),
                        style = MaterialTheme.typography.body1,
                        color = theme.textColor
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { navController.navigate(Screen.AchievementsScreen.route) },
                        modifier = Modifier.size(28.dp)  // compact touch target but not tiny
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_emoji_events_24), // 🏅 medal icon
                            contentDescription = stringResource(R.string.achv_title),
                            tint = Color(0xFFFFD54F) // playful gold accent
                        )
                    }
                }
            }

            // Right-side actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onFeedbackClick != null) {
                    IconButton(onClick = onFeedbackClick) {
                        Icon(
                            imageVector = Icons.Filled.Feedback,
                            contentDescription = stringResource(R.string.main_cd_feedback),
                            tint = Color.Yellow
                        )
                    }
                }
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = stringResource(R.string.main_cd_logout),
                        tint = theme.textColor
                    )
                }
            }
        }

        Divider(
            color = dividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // ===== Main row: Avatar | vertical divider | Mood =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.common_avatar),
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(6.dp))
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(enabled = ownedAvatarIds.isNotEmpty()) {
                            showAvatarDialog = true
                        }
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(dividerColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.common_mood),
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(6.dp))
                MoodStatusCardContent(
                    user = user,
                    onMoodSubmit = onMoodSubmit,
                    onMoodIconSelected = onMoodIconSelected,
                    theme = theme
                )
            }
        }
    }

    if (showAvatarDialog) {
        AvatarPickerDialog(
            ownedAvatarIds = ownedAvatarIds,
            currentAvatarId = user?.avatarId,
            onSelect = { id -> onAvatarSelected(id) },
            onDismiss = { showAvatarDialog = false },
            theme = theme
        )
    }
}
