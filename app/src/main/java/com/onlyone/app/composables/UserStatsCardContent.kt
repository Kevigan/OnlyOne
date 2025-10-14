package com.onlyone.app.composables

import com.onlyone.app.data.UserComposite
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
import com.onlyone.app.Screen
import com.onlyone.app.R
import com.onlyone.app.composables.mapAvatarIdToDrawable
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.ui.avatar.AvatarPickerDialog

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

                    // vertical separator between gold and the button
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    Spacer(Modifier.width(8.dp))

                    // medal button with elevation + circular shape
                    Surface(
                        shape = CircleShape,
                        elevation = 6.dp,
                        color = Color.White.copy(alpha = 0.08f), // subtle fill so it reads as a button
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Screen.AchievementsScreen.route) },
                            modifier = Modifier
                                .size(36.dp) // keeps a comfortable touch target
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_emoji_events_24),
                                contentDescription = stringResource(R.string.achv_title),
                                tint = Color(0xFFFFD54F)
                            )
                        }
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp)) // rounds the box edges
                    .background(Color.White.copy(alpha = 0.08f)) // subtle translucent background
                    .clickable(enabled = ownedAvatarIds.isNotEmpty()) {
                        showAvatarDialog = true
                    }
                    .padding(horizontal = 1.dp, vertical = 1.dp) // space inside box
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.common_avatar),
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_24),
                            contentDescription = null,
                            tint = theme.textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
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
                /*Text(
                    text = stringResource(R.string.common_mood),
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.8f)
                )*/
                //Spacer(Modifier.height(6.dp))
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

