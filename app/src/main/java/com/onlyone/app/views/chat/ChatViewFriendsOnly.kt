package com.onlyone.app.views.chat

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.composables.mapAvatarIdToDrawable
import com.onlyone.app.data.Message
import com.onlyone.app.data.MessageResult
import com.onlyone.app.data.PublicUser
import com.onlyone.app.data.UserComposite
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.buildMessageId
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel

// Prewritten
import com.onlyone.app.prewritten.PrewrittenRepository
import com.onlyone.app.prewritten.PreMsgCategory
import com.onlyone.app.views.chat.components.PrewrittenPickerSheet

@Composable
fun ChatViewFriendsOnly(
    user: UserComposite,
    friend: PublicUser,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavController,
    theme: ThemeTokens,

    // ✅ Optional overrides (useful when the local list has the values even if friend lacks them)
    ageOverride: Int? = null,
    genderOverride: String? = null,
    cityOverride: String? = null,
    achievementCountOverride: Int? = null
) {
    val context = LocalContext.current

    // --- Button themes ---
    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonBackgroundColor,
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )
    val buttonColorsSend = ButtonDefaults.buttonColors(
        backgroundColor = Color(0xFF81C784),
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )

    // ---- Suggestions dialog state ----
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedCat: PreMsgCategory? by rememberSaveable { mutableStateOf(null) } // null => All

    val messagesForDialog by remember(selectedCat, friend.username, user.maxMessageLength) {
        mutableStateOf(
            if (selectedCat == null) {
                PrewrittenRepository.categories()
                    .flatMap {
                        PrewrittenRepository.messagesFor(
                            context = context,
                            category = it,
                            receiverName = friend.username,
                            maxLength = Int.MAX_VALUE
                        )
                    }
                    .distinct()
            } else {
                PrewrittenRepository.messagesFor(
                    context = context,
                    category = selectedCat!!,
                    receiverName = friend.username,
                    maxLength = Int.MAX_VALUE
                )
            }
        )
    }

    // ---- Chat State ----
    var messageText by rememberSaveable { mutableStateOf("") }
    val isSending by chatViewModel.isSending.collectAsState()
    val maxLength = user.maxMessageLength

    // ✅ Resolve profile details (prefer overrides)
    val resolvedAchievement = achievementCountOverride ?: friend.achievementCount ?: 0
    val resolvedFavMsg = friend.favouriteMessage?.text
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.chat_msg_no_fav)

    val resolvedAge = ageOverride ?: friend.age
    val resolvedGender = genderOverride ?: friend.gender
    val resolvedCity = cityOverride ?: friend.city

    val ageStr = resolvedAge?.takeIf { it in 1..99 }?.toString() ?: "-"
    val genderStr = when (resolvedGender?.trim()?.lowercase()) {
        "m", "male" -> "M"
        "f", "female" -> "F"
        "d", "diverse", "other", "nonbinary", "non-binary", "x" -> "D"
        else -> "-"
    }
    val cityStr = resolvedCity?.takeIf { it.isNotBlank() } ?: "-"

    val ageLabel = stringResource(R.string.onboarding_age_label)
    val genderLabel = stringResource(R.string.onboarding_gender_label)
    val cityLabel = stringResource(R.string.onboarding_city_label)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
    ) {
        // Header (same look, no swipes/help)
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 = Color(0xFF001F54).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            theme = theme,
            onDismiss = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.chat_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Main card (same shell, no add-friend)
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 = Color(0xFFF5DEB3).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFFDAA520).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            theme = theme,
            onDismiss = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Friend header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = mapAvatarIdToDrawable(friend.avatarId ?: 0)),
                        contentDescription = stringResource(R.string.chat_cd_avatar),
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = friend.username.ifBlank { stringResource(R.string.chat_username_placeholder) },
                            style = MaterialTheme.typography.subtitle1,
                            color = theme.textColor
                        )
                        Text(
                            text = friend.moodStatus ?: "",
                            style = MaterialTheme.typography.body2,
                            color = theme.textColor
                        )

                        // ✅ Achievements + Fav message (resolved)
                        Text(
                            text = "${stringResource(R.string.achv_title)}: $resolvedAchievement",
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${stringResource(R.string.chat_msg_title)}: $resolvedFavMsg",
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // ✅ Age / Gender / City (resolved with overrides if provided)
                        Text(
                            text = "$ageLabel: $ageStr   $genderLabel: $genderStr   $cityLabel: $cityStr",
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                        color = theme.textColor.copy(alpha = 0.75f),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .align(Alignment.End)
                    )
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = null,
                        placeholder = {
                            Text(
                                stringResource(R.string.chat_placeholder),
                                color = theme.textColor.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = theme.textFieldColor,
                            focusedBorderColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                            cursorColor = theme.textColor,
                            textColor = theme.textColor
                        ),
                        maxLines = Int.MAX_VALUE
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Send button (same spot as original)
                Button(
                    onClick = {
                        val trimmed = messageText.trim()
                        if (trimmed.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.chat_toast_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (trimmed.length > maxLength) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.chat_toast_too_long),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val messageId = buildMessageId(user.uid, friend.uid)
                        val msg = Message(
                            id = messageId,
                            senderUsername = user.username,
                            senderId = user.uid,
                            receiverId = friend.uid,
                            content = trimmed,
                            timestamp = Timestamp.now(),
                            senderAvatarId = user.avatarId,
                            senderMood = user.moodStatus,
                            feedback = -10
                        )

                        chatViewModel.sendMessage(msg, userViewModel = userViewModel) { result ->
                            when (result) {
                                is MessageResult.Success -> {
                                    messageText = ""
                                    val rewardText = context.getString(R.string.chat_reward_text, result.gold)
                                    val runeText = result.rune?.let {
                                        "\n" + context.getString(R.string.chat_rune_drop, it)
                                    } ?: ""
                                    Toast.makeText(context, rewardText + runeText, Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                }
                                is MessageResult.AlreadySent -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.chat_already_sent),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                is MessageResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.chat_send_error),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {}
                            }
                        }
                    },
                    colors = buttonColorsSend,
                    enabled = messageText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = theme.textColor,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.chat_send_button), fontSize = 18.sp, color = theme.textColor)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Prewritten button OUTSIDE the main card (full width)
        Button(
            onClick = { showSuggestions = true },
            colors = buttonColors,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = stringResource(R.string.pre_msg_button), color = theme.textColor)
        }
    }

    // ---- Prewritten suggestions dialog ----
    if (showSuggestions) {
        Dialog(
            onDismissRequest = { showSuggestions = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.98f)
                    .widthIn(max = 720.dp)
            ) {
                CustomColorOverlay(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 4),
                    overlayColor = Color.Gray,
                    onDismiss = {},
                    paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    paddingBox2 = PaddingValues(6.dp),
                    theme = theme
                ) {
                    PrewrittenPickerSheet(
                        theme = theme,
                        categories = PrewrittenRepository.categories(),
                        includeAllOption = true,
                        selected = selectedCat,                  // null => All
                        onSelectCategory = { selectedCat = it }, // set null for All
                        messages = messagesForDialog,
                        onInsert = { msg ->
                            messageText = msg
                            Toast.makeText(
                                context,
                                context.getString(R.string.pre_msg_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                            showSuggestions = false
                        }
                    )
                }
            }
        }
    }
}
