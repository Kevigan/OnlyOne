package com.example.onlyone.composables

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

    // expand behavior
    expanded: Boolean = false,
    onCardClick: (() -> Unit)? = null,

    // NEW: full object + count
    favouriteMessage: FavouriteMessage? = null,
    achievementCount: Int = 0
) {
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var blockDialogVisible by remember { mutableStateOf(false) }
    var unblockDialogVisible by remember { mutableStateOf(false) }

    // ⭐ dialog states
    var favDialogVisible by remember { mutableStateOf(false) }
    var achDialogVisible by remember { mutableStateOf(false) }

    val compactHeight = 72.dp
    val expandedHeight = 108.dp
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
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = if (expanded) 3 else 1,
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
                            contentDescription = "Show favourite message",
                            modifier = Modifier
                                .size(32.dp)                 // control overall size
                                .clip(CircleShape),          // ⬅️ makes it round
                            contentScale = ContentScale.Crop // ensures it fills the circle
                        )
                    }
                    IconButton(onClick = { achDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents, // 🏆 built-in trophy
                            contentDescription = "Show achievements",
                            tint = Color.Yellow
                        )
                    }
                }
            }

            // ───── Dialogs ──────────────────────────────────────────────

            // Delete confirmation
            if (deleteDialogVisible) {
                AlertDialog(
                    onDismissRequest = { deleteDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_delete_title)) },
                    text = { Text(stringResource(R.string.friends_dialog_delete_text, name)) },
                    confirmButton = {
                        TextButton(onClick = {
                            deleteDialogVisible = false
                            onDelete?.invoke()
                        }) { Text(stringResource(R.string.common_confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogVisible = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // Block confirmation
            if (blockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { blockDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_block_title)) },
                    text = { Text(stringResource(R.string.friends_dialog_block_text, name)) },
                    confirmButton = {
                        TextButton(onClick = {
                            blockDialogVisible = false
                            onBlock?.invoke()
                        }) { Text(stringResource(R.string.friends_dialog_block_confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { blockDialogVisible = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // Unblock / Unblock & request
            if (unblockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { unblockDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_unblock_title)) },
                    text = { Text(stringResource(R.string.friends_dialog_unblock_text, name)) },
                    confirmButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblock?.invoke()
                        }) { Text(stringResource(R.string.friends_dialog_unblock_confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblockAndRequest?.invoke()
                        }) { Text(stringResource(R.string.friends_dialog_unblock_and_request)) }
                    }
                )
            }

            // ⭐ Favourite message dialog (uses the object)
            if (favDialogVisible) {
                AlertDialog(
                    onDismissRequest = { favDialogVisible = false },
                    title = { Text("Favourite message") },
                    text = {
                        Text(
                            favouriteMessage?.text?.takeIf { it.isNotBlank() }
                                ?: "No favourite message yet."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { favDialogVisible = false }) { Text("OK") }
                    }
                )
            }

            // ⭐ Achievement count dialog
            if (achDialogVisible) {
                AlertDialog(
                    onDismissRequest = { achDialogVisible = false },
                    title = { Text("Achievements") },
                    text = { Text("Total achievements: $achievementCount") },
                    confirmButton = {
                        TextButton(onClick = { achDialogVisible = false }) { Text("OK") }
                    }
                )
            }
        }
    }
}
