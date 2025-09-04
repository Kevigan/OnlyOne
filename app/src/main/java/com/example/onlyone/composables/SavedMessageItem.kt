package com.example.onlyone.composables

import CustomAlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.unit.sp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun SavedMessageItem(
    theme: ThemeTokens,
    avatarResId: Int,
    name: String,
    message: String,
    isFavorite: Boolean,
    isProcessing: Boolean,
    onStarClick: () -> Unit,
    onDeleteConfirm: () -> Unit,   // ← NEW
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(percent = 45),
        overlayColor = Color.Gray,
        borderColor = Color.White,
        theme = theme,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp),
        borderWidth = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Avatar for $name",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = name,
                        style = MaterialTheme.typography.body1,
                        color = theme.textColor
                    )
                }

                // Right actions: spinner/star + delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = theme.textColor
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = if (isFavorite)
                                    R.drawable.favourite_message_icon
                                else
                                    R.drawable.favourite_message_icon_grey
                            ),
                            contentDescription = if (isFavorite) "Selected favourite" else "Set as favourite",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape) // ⬅ make it round
                                .clickable(enabled = !isProcessing) { onStarClick() }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = stringResource(R.string.cd_delete_saved),
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(enabled = !isProcessing) {
                                showDeleteDialog = true
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                fontSize = 16.sp,
                color = theme.textColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Delete saved item dialog (with CustomAlertDialog)
    if (showDeleteDialog) {
        CustomAlertDialog(
            theme = theme,
            borderColor = theme.borderColor,
            onDismiss = { showDeleteDialog = false }
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.dialog_delete_saved_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.dialog_delete_saved_text),
                    style = MaterialTheme.typography.body1,
                    color = theme.textColor
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.common_no), color = theme.textColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    }) {
                        Text(stringResource(R.string.common_yes), color = Color.Red)
                    }
                }
            }
        }
    }
}
