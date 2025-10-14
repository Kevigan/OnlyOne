package com.onlyone.app.views.chat

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.data.PublicUser
import com.onlyone.app.data.UserComposite
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.onlyone.app.R
import com.onlyone.app.composables.mapAvatarIdToDrawable
import com.onlyone.app.data.Message
import com.onlyone.app.utils.buildMessageId
import com.google.firebase.Timestamp
import com.onlyone.app.data.MessageResult

@Composable
fun UserCardContent(
    theme: ThemeTokens,
    appUser: UserComposite,                 // the signed-in user
    target: PublicUser,                     // the profile shown on the card
    messageText: String,
    onMessageChange: (String) -> Unit,
    isSending: Boolean,
    enableInput: Boolean,
    enableSend: Boolean,
    isRandom: Boolean,
    showAddFriend: Boolean,
    onTapAddFriend: () -> Unit,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val themeText = theme.textColor
    val maxLength = appUser.maxMessageLength

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header (avatar + texts + add-friend)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = mapAvatarIdToDrawable(target.avatarId ?: 0)),
                    contentDescription = stringResource(R.string.chat_cd_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = target.username ?: stringResource(R.string.chat_username_placeholder),
                        style = MaterialTheme.typography.subtitle1,
                        color = themeText
                    )
                    Text(
                        text = target.moodStatus ?: "",
                        style = MaterialTheme.typography.body2,
                        color = themeText
                    )
                    val achievementCount = target.achievementCount ?: 0
                    Text(
                        text = "${stringResource(R.string.achv_title)}: $achievementCount",
                        style = MaterialTheme.typography.caption,
                        color = themeText.copy(alpha = 0.8f)
                    )
                    val favMsg = target.favouriteMessage?.text
                        ?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.chat_msg_no_fav)
                    Text(
                        text = "${stringResource(R.string.chat_msg_title)}: $favMsg",
                        style = MaterialTheme.typography.caption,
                        color = themeText.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val ageStr = target.age?.takeIf { it in 1..99 }?.toString() ?: "-"
                    val genderStr = when (target.gender?.trim()?.lowercase()) {
                        "m", "male" -> "M"
                        "f", "female" -> "F"
                        "d", "diverse", "other", "nonbinary", "non-binary", "x" -> "D"
                        else -> "-"
                    }
                    val cityStr = target.city?.takeIf { it.isNotBlank() } ?: "-"

                    val ageLabel = stringResource(R.string.onboarding_age_label)
                    val genderLabel = stringResource(R.string.onboarding_gender_label)
                    val cityLabel = stringResource(R.string.onboarding_city_label)

                    Text(
                        text = "$ageLabel: $ageStr   $genderLabel: $genderStr   $cityLabel: $cityStr",
                        style = MaterialTheme.typography.caption,
                        color = themeText.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showAddFriend) {
                IconButton(
                    onClick = onTapAddFriend,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(54.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_add_24),
                        contentDescription = stringResource(R.string.friends_cd_add),
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Message area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = stringResource(R.string.chat_input_label, messageText.length, maxLength),
                style = MaterialTheme.typography.caption,
                color = themeText.copy(alpha = 0.75f),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .align(Alignment.End)
            )
            OutlinedTextField(
                value = messageText,
                onValueChange = { if (enableInput) onMessageChange(it) },
                label = null,
                placeholder = { Text(stringResource(R.string.chat_placeholder), color = themeText.copy(alpha = 0.6f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(8.dp),
                enabled = enableInput,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = theme.textFieldColor,
                    focusedBorderColor = themeText,
                    unfocusedBorderColor = themeText.copy(alpha = 0.6f),
                    cursorColor = themeText,
                    textColor = themeText,
                    disabledTextColor = themeText.copy(alpha = 0.6f),
                    disabledBorderColor = themeText.copy(alpha = 0.4f),
                    disabledLabelColor = themeText.copy(alpha = 0.5f)
                ),
                maxLines = Int.MAX_VALUE
            )
        }

        Spacer(Modifier.height(12.dp))

        // Send button
        val buttonColorsSend = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF81C784),
            contentColor = themeText,
            disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
            disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
        )
        Button(
            onClick = {
                val trimmed = messageText.trim()
                if (trimmed.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.chat_toast_empty), Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (trimmed.length > maxLength) {
                    Toast.makeText(context, context.getString(R.string.chat_toast_too_long), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val receiverId = target.uid ?: return@Button
                val messageId = buildMessageId(appUser.uid, receiverId)

                val msg = Message(
                    id = messageId,
                    senderUsername = appUser.username,
                    senderId = appUser.uid,
                    receiverId = receiverId,
                    content = trimmed,
                    timestamp = Timestamp.now(),
                    senderAvatarId = appUser.avatarId,
                    senderMood = appUser.moodStatus,
                    feedback = -10
                )

                chatViewModel.sendMessage(msg, userViewModel = userViewModel) { result ->
                    when (result) {
                        is MessageResult.Success -> {
                            onMessageChange("")
                            val rewardText = context.getString(R.string.chat_reward_text, result.gold)
                            val runeText = result.rune?.let { "\n" + context.getString(R.string.chat_rune_drop, it) } ?: ""
                            Toast.makeText(context, rewardText + runeText, Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                        is MessageResult.AlreadySent -> {
                            Toast.makeText(context, context.getString(R.string.chat_already_sent), Toast.LENGTH_LONG).show()
                        }
                        is MessageResult.Error -> {
                            Toast.makeText(context, context.getString(R.string.chat_send_error), Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            },
            colors = buttonColorsSend,
            enabled = enableSend,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    color = themeText,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.chat_send_button), fontSize = 18.sp, color = themeText)
            }
        }
    }
}
