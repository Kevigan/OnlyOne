package com.example.onlyone.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.UserComposite
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.utils.buildMessageId
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@Composable
fun ChatView(
    user: UserComposite,
    targetUser: PublicUser?,
    isRandom: Boolean,
    onNextUser: () -> Unit,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavController
) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isSending by chatViewModel.isSending.collectAsState()
    val maxLength = user.maxMessageLength
    val coroutineScope = rememberCoroutineScope()
    val engagementStatus by userViewModel.engagementStatus.collectAsState()

    var selectedLanguage by remember { mutableStateOf("any") }

    LaunchedEffect(user.uid) {
        userViewModel.getSearchUserLanguage { savedLang -> selectedLanguage = savedLang }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.chat_title),
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isRandom && engagementStatus != null) {
            val swipesUsed = engagementStatus!!.swipesUsed
            val maxSwipes = user.maxSwipes
            Text(
                text = stringResource(R.string.chat_swipes, swipesUsed, maxSwipes),
                style = MaterialTheme.typography.subtitle1,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Public Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = mapAvatarIdToDrawable(targetUser?.avatarId ?: 0)
                    ),
                    contentDescription = stringResource(R.string.chat_cd_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = targetUser?.username ?: stringResource(R.string.chat_username_placeholder),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = targetUser?.moodStatus ?: "",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (targetUser != null) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { if (it.length <= maxLength) messageText = it },
                    label = { Text(stringResource(R.string.chat_input_label, messageText.length, maxLength)) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ghosthead_sad),
                        contentDescription = stringResource(R.string.chat_cd_waiting),
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 12.dp)
                    )
                    Text(stringResource(R.string.chat_searching), style = MaterialTheme.typography.body1)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (!isRandom) 32.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (messageText.trim().length > user.maxMessageLength) {
                        Toast.makeText(context, context.getString(R.string.chat_toast_too_long), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val messageId = buildMessageId(user.uid, targetUser?.uid ?: "")
                    val msg = Message(
                        id = messageId,
                        senderUsername = user.username,
                        senderId = user.uid,
                        receiverId = targetUser?.uid ?: "",
                        content = messageText.trim(),
                        timestamp = Timestamp.now(),
                        senderAvatarId = user.avatarId,
                        senderMood = user.moodStatus,
                        feedback = -10
                    )

                    chatViewModel.sendMessage(msg, userViewModel = userViewModel) { result ->
                        when (result) {
                            is MessageResult.Success -> {
                                messageText = ""
                                val rewardText = context.getString(R.string.chat_reward_text, result.gold, result.points)
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
                        }
                    }
                },
                enabled = messageText.isNotBlank() && !isSending,
                modifier = Modifier
                    .defaultMinSize(minWidth = 160.dp, minHeight = 48.dp)
                    .fillMaxWidth()
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.chat_send_button), fontSize = 18.sp)
                }
            }
        }

        // Language filter + next user
        if (isRandom) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                val languageOptions = listOf("any", "en", "de", "fr", "es", "it")

                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(stringResource(R.string.chat_language_prefix, selectedLanguage.uppercase()))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languageOptions.forEach { lang ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = lang
                                    expanded = false
                                    userViewModel.saveSearchUserLanguage(lang)
                                    coroutineScope.launch {
                                        userViewModel.loadRandomUserBatch(showToasts = true)
                                    }
                                }
                            ) {
                                Text(lang.uppercase())
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNextUser,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = stringResource(R.string.chat_cd_next_user),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
