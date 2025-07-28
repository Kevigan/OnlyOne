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
    onBlock: (() -> Unit)? = null, // ✅ new block callback
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
                contentDescription = "Avatar for $name",
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

            // 💬 Write
            if (onWriteClick != null) {
                if (isLocked) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_lock_clock_24),
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(onClick = onWriteClick) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_message_24),
                            contentDescription = "Write",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.Green)
                        )
                    }
                }
                // ❌ Delete icon
                if (showDelete && onDelete != null) {
                    IconButton(onClick = { deleteDialogVisible = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Friend",
                            tint = Color.Red
                        )
                    }
                }
            }

            // 🛑 Block icon
            if (onBlock != null) {
                IconButton(onClick = { blockDialogVisible = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_block_24),
                        contentDescription = "Block User",
                        tint = Color.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (onUnblock != null || onUnblockAndRequest != null) {
                IconButton(onClick = { unblockDialogVisible = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_block_24),
                        contentDescription = "Unblock User",
                        tint = Color.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ✅ Accept / ❌ Decline buttons
            if (showAccept && onAccept != null) {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
                }
            }

            if (showDecline && onDecline != null) {
                IconButton(onClick = onDecline) {
                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Red)
                }
            }

            // 🧨 Delete Confirmation Dialog
            if (deleteDialogVisible) {
                AlertDialog(
                    onDismissRequest = { deleteDialogVisible = false },
                    title = { Text("Delete Friend") },
                    text = { Text("Are you sure you want to delete $name from your friend list?") },
                    confirmButton = {
                        TextButton(onClick = {
                            deleteDialogVisible = false
                            onDelete?.invoke()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogVisible = false }) {
                            Text("No")
                        }
                    }
                )
            }

            if (blockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { blockDialogVisible = false },
                    title = { Text("Block User") },
                    text = {
                        Text("Are you sure you want to block $name? This will also remove them from your friends list if they are a friend.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            blockDialogVisible = false
                            onBlock?.invoke()
                        }) {
                            Text("Block", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { blockDialogVisible = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (unblockDialogVisible) {
                AlertDialog(
                    onDismissRequest = { unblockDialogVisible = false },
                    title = { Text("Unblock User") },
                    text = {
                        Text("Do you want to unblock $name?\n\nYou can also send them a friend request immediately after unblocking.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblock?.invoke()
                        }) {
                            Text("Unblock")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            unblockDialogVisible = false
                            onUnblockAndRequest?.invoke()
                        }) {
                            Text("Unblock + Request")
                        }
                    }
                )
            }
        }
    }
}


