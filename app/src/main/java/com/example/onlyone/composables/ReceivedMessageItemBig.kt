package com.example.onlyone.composables

import CustomAlertDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ReceivedMessageItemBig(
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    message: LocalMessage,
    onFeedbackSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Int = 45,
    theme: ThemeTokens
) {
    val context = LocalContext.current
    var showBlockDialog by remember { mutableStateOf(false) }

    // Observe real local favourites (Room) to reflect bookmark state
    val favorites by chatViewModel.observeFavoriteMessages().collectAsState(initial = emptyList())
    val isSaved by remember(favorites, message.id) {
        derivedStateOf { favorites.any { it.id == message.id } }
    }

    LaunchedEffect(message.id) {
        chatViewModel.markMessageAsRead(message)
    }

    CustomColorOverlay(
        modifier = modifier,
        shape = RoundedCornerShape(shape),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(10.dp),
        theme = theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message.content,
                color = theme.textColor,
                style = MaterialTheme.typography.body1,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            if (message.feedback == -10) {
                Text(
                    text = stringResource(R.string.main_give_feedback),
                    style = MaterialTheme.typography.subtitle1,
                    color = theme.textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_thumb_up_off_alt_24),
                        contentDescription = stringResource(R.string.cd_thumb_up),
                        tint = Color.Green,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(1) }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_sentiment_neutral_24),
                        contentDescription = stringResource(R.string.cd_neutral),
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(0) }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_thumb_down_off_alt_24),
                        contentDescription = stringResource(R.string.cd_thumb_down),
                        tint = Color.Red,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(-1) }
                    )
                }
            } else {
                val (iconId, tint, description) = when (message.feedback) {
                    1 -> Triple(
                        R.drawable.baseline_thumb_up_off_alt_24,
                        Color.Green,
                        stringResource(R.string.feedback_you_gave_thumbs_up)
                    )
                    0 -> Triple(
                        R.drawable.baseline_sentiment_neutral_24,
                        Color.Gray,
                        stringResource(R.string.feedback_you_gave_neutral)
                    )
                    -1 -> Triple(
                        R.drawable.baseline_thumb_down_off_alt_24,
                        Color.Red,
                        stringResource(R.string.feedback_you_gave_thumbs_down)
                    )
                    else -> Triple(
                        R.drawable.baseline_sentiment_neutral_24,
                        Color.LightGray,
                        stringResource(R.string.feedback_none)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = description,
                    tint = tint,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = theme.textColor,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save/Unsave + Block row
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 📑 Local save/unsave toggle (reactive)
                Icon(
                    painter = painterResource(
                        id = if (isSaved) R.drawable.baseline_bookmark_24
                        else R.drawable.baseline_bookmark_border_24
                    ),
                    contentDescription = stringResource(
                        if (isSaved) R.string.cd_unsave_locally else R.string.cd_save_locally
                    ),
                    tint = if (isSaved) Color.Cyan else Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            if (isSaved) {
                                chatViewModel.removeFavorite(message.id)
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.toast_removed_from_saved),
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            } else {
                                chatViewModel.saveMessageToFavorites(message)
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.toast_saved_locally),
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.baseline_block_24),
                    contentDescription = stringResource(R.string.cd_block_user),
                    tint = Color.Yellow,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { showBlockDialog = true }
                )
            }
        }

        if (showBlockDialog) {
            CustomAlertDialog(
                borderColor = Color.Yellow,
                theme = theme,
                onDismiss = { showBlockDialog = false }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.dialog_block_title),
                        style = MaterialTheme.typography.h6,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.dialog_block_text),
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showBlockDialog = false }) {
                            Text(stringResource(R.string.common_cancel), color = theme.textColor)
                        }
                        TextButton(onClick = {
                            showBlockDialog = false
                            userViewModel.blockUser(message.senderId)
                        }) {
                            Text(stringResource(R.string.common_block), color = Color.Yellow)
                        }
                    }
                }
            }
        }

    }
}
