package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R

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
    onUnblockAndRequest: (() -> Unit)? = null
) {
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var blockDialogVisible by remember { mutableStateOf(false) }
    var unblockDialogVisible by remember { mutableStateOf(false) }

    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 45),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(vertical = 1.dp),
        paddingBox2 = PaddingValues(vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 1.dp),
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
                    color = Color.White
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.7f)
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

            // 🛑 Block
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

            // ✅ Unblock / request
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

            // ✅ Accept / ❌ Decline (for requests)
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

            // 🧨 Delete confirmation
            if (deleteDialogVisible) {
                AlertDialog(
                    onDismissRequest = { deleteDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_delete_title)) },
                    text = {
                        Text(stringResource(R.string.friends_dialog_delete_text, name))
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            deleteDialogVisible = false
                            onDelete?.invoke()
                        }) {
                            Text(stringResource(R.string.common_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogVisible = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // 🛑 Block confirmation
            if (blockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { blockDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_block_title)) },
                    text = {
                        Text(stringResource(R.string.friends_dialog_block_text, name))
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            blockDialogVisible = false
                            onBlock?.invoke()
                        }) {
                            Text(stringResource(R.string.friends_dialog_block_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { blockDialogVisible = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // 🔓 Unblock
            if (unblockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { unblockDialogVisible = false },
                    title = { Text(stringResource(R.string.friends_dialog_unblock_title)) },
                    text = {
                        Text(stringResource(R.string.friends_dialog_unblock_text, name))
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblock?.invoke()
                        }) {
                            Text(stringResource(R.string.friends_dialog_unblock_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblockAndRequest?.invoke()
                        }) {
                            Text(stringResource(R.string.friends_dialog_unblock_and_request))
                        }
                    }
                )
            }
        }
    }
}



