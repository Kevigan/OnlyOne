package com.example.onlyone.composables

import CustomAlertDialog
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person // <-- profile icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.data.FavouriteMessage
import com.example.onlyone.theme.ThemeTokens

@Composable
fun FriendItem(
    avatarResId: Int,
    name: String,
    status: String,
    isLocked: Boolean = true,
    showAccept: Boolean = false,
    showDecline: Boolean = false,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onWriteClick: (() -> Unit)? = null,
    showDelete: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onBlock: (() -> Unit)? = null,
    onUnblock: (() -> Unit)? = null,
    onUnblockAndRequest: (() -> Unit)? = null,
    theme: ThemeTokens,
    // expand behavior
    expanded: Boolean = false,
    onCardClick: (() -> Unit)? = null,

    // full object + count
    favouriteMessage: FavouriteMessage? = null,
    achievementCount: Int = 0,

    // details for dialog
    age: Int? = null,
    gender: String? = null, // "m" | "f" | "d"
    city: String? = null
) {
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var blockDialogVisible by remember { mutableStateOf(false) }
    var unblockDialogVisible by remember { mutableStateOf(false) }

    // ⭐ dialog states
    var favDialogVisible by remember { mutableStateOf(false) }
    var achDialogVisible by remember { mutableStateOf(false) }
    var detailsDialogVisible by remember { mutableStateOf(false) } // <-- profile details

    val compactHeight = 72.dp
    val expandedHeight = 128.dp
    val targetHeight = if (expanded) expandedHeight else compactHeight
    val animatedHeight by animateDpAsState(targetValue = targetHeight, label = "friendItemHeight")
    val cornerShape = if (expanded) RoundedCornerShape(20.dp) else RoundedCornerShape(percent = 45)

    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier),
        shape = cornerShape,
        overlayColor = Color.Gray,
        theme = theme,
        onDismiss = {},
        paddingBox1 = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
        paddingBox2 = PaddingValues(vertical = 6.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 👤 Avatar
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = stringResource(R.string.friends_cd_avatar_for, name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name + Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.body1,
                        color = theme.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.caption,
                        color = theme.textColor.copy(alpha = 0.7f),
                        maxLines = if (expanded) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 💬 Write / delete
                if (onWriteClick != null) {
                    if (isLocked) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_lock_clock_24),
                            contentDescription = stringResource(R.string.friends_cd_locked),
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        IconButton(onClick = onWriteClick) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_message_24),
                                contentDescription = stringResource(R.string.friends_cd_write),
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color.Green)
                            )
                        }
                    }
                    if (showDelete && onDelete != null) {
                        IconButton(onClick = { deleteDialogVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.friends_cd_delete_friend),
                                tint = Color.Red
                            )
                        }
                    }
                }

                // 🚫 Block
                if (onBlock != null) {
                    IconButton(onClick = { blockDialogVisible = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_block_24),
                            contentDescription = stringResource(R.string.friends_cd_block_user),
                            tint = Color.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 🔓 Unblock / Unblock+Request
                if (onUnblock != null || onUnblockAndRequest != null) {
                    IconButton(onClick = { unblockDialogVisible = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_block_24),
                            contentDescription = stringResource(R.string.friends_cd_unblock_user),
                            tint = Color.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ✅/❌ Friend requests
                if (showAccept && onAccept != null) {
                    IconButton(onClick = onAccept) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.friends_cd_accept),
                            tint = Color.Green
                        )
                    }
                }
                if (showDecline && onDecline != null) {
                    IconButton(onClick = onDecline) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.friends_cd_decline),
                            tint = Color.Red
                        )
                    }
                }
            }

            // ⭐ Extra actions row when expanded (opens our dialogs)
            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { favDialogVisible = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.favourite_message_icon),
                            contentDescription = stringResource(R.string.friends_cd_show_favourite),
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(onClick = { achDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = stringResource(R.string.friends_cd_show_achievements),
                            tint = Color.Yellow
                        )
                    }
                    IconButton(onClick = { detailsDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = stringResource(R.string.friends_cd_profile),
                            tint = Color.Cyan
                        )
                    }
                }
            }

            // ───── Dialogs ──────────────────────────────────────────────

            // Delete confirmation (RED)
            if (deleteDialogVisible) {
                CustomAlertDialog(
                    borderColor = Color.Red,
                    theme = theme,
                    onDismiss = { deleteDialogVisible = false }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.friends_dialog_delete_title),
                            style = MaterialTheme.typography.h6,
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.friends_dialog_delete_text, name),
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { deleteDialogVisible = false }) {
                                Text(stringResource(R.string.common_cancel))
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                deleteDialogVisible = false
                                onDelete?.invoke()
                            }) {
                                Text(stringResource(R.string.common_confirm), color = Color.Red)
                            }
                        }
                    }
                }
            }

            // Block confirmation (YELLOW)
            if (blockDialogVisible) {
                CustomAlertDialog(
                    borderColor = Color.Yellow,
                    theme = theme,
                    onDismiss = { blockDialogVisible = false }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.friends_dialog_block_title),
                            style = MaterialTheme.typography.h6,
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.friends_dialog_block_text, name),
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { blockDialogVisible = false }) {
                                Text(stringResource(R.string.common_cancel), color = theme.textColor)
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                blockDialogVisible = false
                                onBlock?.invoke()
                            }) {
                                Text(stringResource(R.string.friends_dialog_block_confirm), color = Color.Yellow)
                            }
                        }
                    }
                }
            }

            // Unblock / Unblock & request (DEFAULT color)
            if (unblockDialogVisible) {
                CustomAlertDialog(theme = theme, onDismiss = { unblockDialogVisible = false }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.friends_dialog_unblock_title),
                            style = MaterialTheme.typography.h6,
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.friends_dialog_unblock_text, name),
                            color = theme.textColor
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                unblockDialogVisible = false
                                onUnblockAndRequest?.invoke()
                            }) {
                                Text(stringResource(R.string.friends_dialog_unblock_and_request), color = Color.Yellow)
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                unblockDialogVisible = false
                                onUnblock?.invoke()
                            }) {
                                Text(stringResource(R.string.friends_dialog_unblock_confirm), color = theme.textColor)
                            }
                        }
                    }
                }
            }

            // Favourite message (DEFAULT color)
            // --- Favourite message dialog
            if (favDialogVisible) {
                CustomAlertDialog(theme = theme, onDismiss = { favDialogVisible = false }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.chat_msg_title), style = MaterialTheme.typography.h6, color = theme.textColor)
                        Spacer(Modifier.height(8.dp))
                        Text(favouriteMessage?.text?.takeIf { it.isNotBlank() } ?: stringResource(R.string.chat_msg_no_fav), color = theme.textColor)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { favDialogVisible = false }) {
                                Text(stringResource(R.string.common_ok), color = theme.textColor)
                            }
                        }
                    }
                }
            }

            // --- Achievements dialog
            if (achDialogVisible) {
                CustomAlertDialog(theme = theme, onDismiss = { achDialogVisible = false }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.friends_dialog_achievements_title), style = MaterialTheme.typography.h6, color = theme.textColor)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.friends_dialog_achievements_total, achievementCount), color = theme.textColor)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { achDialogVisible = false }) {
                                Text(stringResource(R.string.common_ok), color = theme.textColor)
                            }
                        }
                    }
                }
            }

            // Profile details (DEFAULT color)
            if (detailsDialogVisible) {
                CustomAlertDialog(theme = theme, onDismiss = { detailsDialogVisible = false }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.friends_dialog_profile_title), style = MaterialTheme.typography.h6, color = theme.textColor)
                        Spacer(Modifier.height(8.dp))

                        val ageStr = age?.takeIf { it in 1..99 }?.toString() ?: "-"
                        val genderStr = when (gender?.lowercase()) {
                            "m" -> "M"
                            "f" -> "F"
                            "d" -> "D"
                            else -> "-"
                        }
                        val cityStr = city?.takeIf { it.isNotBlank() } ?: "-"

                        val ageLabel = stringResource(R.string.onboarding_age_label)
                        val genderLabel = stringResource(R.string.onboarding_gender_label)
                        val cityLabel = stringResource(R.string.onboarding_city_label)

                        Text("$ageLabel: $ageStr", color = theme.textColor)
                        Spacer(Modifier.height(4.dp))
                        Text("$genderLabel: $genderStr", color = theme.textColor)
                        Spacer(Modifier.height(4.dp))
                        Text("$cityLabel: $cityStr", color = theme.textColor)

                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { detailsDialogVisible = false }) { Text("OK") }
                        }
                    }
                }
            }
        }
    }
}
