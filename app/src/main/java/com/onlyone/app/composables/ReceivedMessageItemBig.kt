package com.onlyone.app.composables

import CustomAlertDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.data.LocalMessage
import com.onlyone.app.data.ReportReason
import com.onlyone.app.data.ReportResult
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.onlyone.app.views.chat.ConfirmFriendRequestDialog

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
    var showReportDialog by remember { mutableStateOf(false) }
    var reasonExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var isReporting by remember { mutableStateOf(false) }

    // Observe local favourites (Room) to reflect bookmark state
    val favorites by chatViewModel.observeFavoriteMessages().collectAsState(initial = emptyList())
    val isSaved by remember(favorites, message.id) {
        derivedStateOf { favorites.any { it.id == message.id } }
    }

    // Mark read on enter
    LaunchedEffect(message.id) {
        chatViewModel.markMessageAsRead(message)
    }

    // Localized reason items (label -> enum)
    val reasonItems = listOf(
        stringResource(R.string.report_reason_racism) to ReportReason.RACISM,
        stringResource(R.string.report_reason_harassment) to ReportReason.HARASSMENT,
        stringResource(R.string.report_reason_sexual) to ReportReason.SEXUAL,
        stringResource(R.string.report_reason_spam) to ReportReason.SPAM,
        stringResource(R.string.report_reason_self_harm) to ReportReason.SELF_HARM,
        stringResource(R.string.report_reason_other) to ReportReason.OTHER
    )

    var showAddFriendDialog by rememberSaveable { mutableStateOf(false) }


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

            // Separator between message and feedback header
            Divider(
                color = theme.textColor.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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

            // Separator between feedback area and action row
            Divider(
                color = theme.textColor.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Save / Block / Report row
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Save / Unsave
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_add_24),
                    contentDescription = stringResource(R.string.friends_cd_add),
                    tint =  Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            showAddFriendDialog = true
                        }
                )

                Spacer(modifier = Modifier.width(16.dp))

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

                // Block
                Icon(
                    painter = painterResource(id = R.drawable.baseline_block_24),
                    contentDescription = stringResource(R.string.cd_block_user),
                    tint = Color.Yellow,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { showBlockDialog = true }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Report
                Icon(
                    painter = painterResource(id = R.drawable.baseline_report_24),
                    contentDescription = stringResource(R.string.cd_report_message),
                    tint = Color(0xFFFF9800),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            selectedReason = null
                            showReportDialog = true
                        }
                )
            }
        }

        // Block dialog
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

        // Report dialog
        if (showReportDialog) {
            CustomAlertDialog(
                borderColor = Color(0xFFFF9800),
                theme = theme,
                onDismiss = { showReportDialog = false }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.report_dialog_title),
                        style = MaterialTheme.typography.h6,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.report_dialog_body),
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(16.dp))

                    // Dropdown selector
                    Box {
                        val label = selectedReason?.let { sel ->
                            reasonItems.firstOrNull { it.second == sel }?.first
                        } ?: stringResource(R.string.report_select_reason)

                        OutlinedButton(
                            onClick = { reasonExpanded = true },
                            enabled = !isReporting
                        ) {
                            Text(label, color = theme.textColor)
                        }
                        DropdownMenu(
                            expanded = reasonExpanded,
                            onDismissRequest = { reasonExpanded = false }
                        ) {
                            reasonItems.forEach { (title, value) ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedReason = value
                                        reasonExpanded = false
                                    }
                                ) {
                                    Text(title)
                                }
                            }
                        }
                    }

                    // Optional loader
                    if (isReporting) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text(stringResource(R.string.common_cancel), color = theme.textColor)
                        }
                        TextButton(
                            onClick = {
                                val reason = selectedReason
                                if (reason != null && !isReporting) {
                                    isReporting = true
                                    chatViewModel.reportMessageAndReturn(message, reason, null) { res ->
                                        isReporting = false
                                        when (res) {
                                            ReportResult.Success -> {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.report_toast_sent),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showReportDialog = false
                                            }
                                            ReportResult.Duplicate -> {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.report_toast_duplicate),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showReportDialog = false
                                            }
                                            ReportResult.Invalid -> {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.report_toast_invalid),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            is ReportResult.Error -> {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.report_toast_error),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            },
                            enabled = selectedReason != null && !isReporting
                        ) {
                            Text(
                                stringResource(R.string.report_send_button),
                                color = if (selectedReason != null && !isReporting)
                                    Color(0xFFFF9800)
                                else
                                    theme.textColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        if (showAddFriendDialog) {
            ConfirmFriendRequestDialog(
                theme = theme,
                onConfirm = {
                    userViewModel.sendFriendRequestDirect(message.senderId) { ok ->
                        Toast.makeText(
                            context,
                            if (ok) context.getString(R.string.friends_toast_request_sent)
                            else context.getString(R.string.friends_unknown),
                            Toast.LENGTH_SHORT
                        ).show()
                        showAddFriendDialog = false
                    }
                },
                onDismiss = { showAddFriendDialog = false }
            )
        }

    }
}
