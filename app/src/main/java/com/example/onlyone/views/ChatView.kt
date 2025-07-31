package com.example.onlyone.views

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.User
import com.example.onlyone.repos.UserRepository
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.utils.buildMessageId
import com.example.onlyone.viewModels.UserViewModel
import com.google.firebase.Timestamp

@Composable
fun ChatView(
    user: User,
    targetUser: PublicUser,
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

    var selectedLanguage by remember { mutableStateOf("any") }

    LaunchedEffect(user.uid) {
        userViewModel.getSearchUserLanguage { savedLang ->
            selectedLanguage = savedLang
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🟢 Header
        Text(
            text = "Send a happy message 😊",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 👤 Public Info Card
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
                    painter = painterResource(id = mapAvatarIdToDrawable(targetUser.avatarId)),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(text = targetUser.username, style = MaterialTheme.typography.subtitle1)
                    Text(text = targetUser.moodStatus, style = MaterialTheme.typography.body2)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 📝 Chat input
        OutlinedTextField(
            value = messageText,
            onValueChange = {
                if (it.length <= maxLength) messageText = it
            },
            label = { Text("Write your message (${messageText.length}/$maxLength)") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 150.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (messageText.trim().length > user.maxMessageLength) {
                        Toast.makeText(context, "Your message is too long!", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    val messageId = buildMessageId(user.uid, targetUser.uid)
                    val msg = Message(
                        id = messageId,
                        senderUsername = user.username,
                        senderId = user.uid,
                        receiverId = targetUser.uid,
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

                                val rewardText =
                                    "Earned: +${result.gold} gold, +${result.points} points"
                                val runeText =
                                    result.rune?.let { "\n🎉 Lucky drop: $it rune!" } ?: ""

                                Toast.makeText(context, "$rewardText$runeText", Toast.LENGTH_LONG)
                                    .show()
                                navController.popBackStack()
                            }

                            is MessageResult.AlreadySent -> {
                                Toast.makeText(
                                    context,
                                    "You already messaged this user today.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            is MessageResult.Error -> {
                                Toast.makeText(
                                    context,
                                    "❌ Couldn't send message. Try again later.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                enabled = messageText.isNotBlank() && !isSending,
                modifier = Modifier
                    .defaultMinSize(minWidth = 160.dp, minHeight = 48.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send", fontSize = 18.sp) // 👈 bigger font
                }
            }
        }

// 🌐 Language filter + next user
        if (isRandom) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder dropdown
                var expanded by remember { mutableStateOf(false) }
                val languageOptions = listOf("any","en", "de", "fr", "es", "it")

                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text("Language: ${selectedLanguage.uppercase()}")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languageOptions.forEach { lang ->
                            DropdownMenuItem(onClick = {
                                selectedLanguage = lang
                                expanded = false

                                userViewModel.saveSearchUserLanguage(lang)

                                // 👇 Clear stale users
                                chatViewModel.clearQueue()

                                // 👇 Then load fresh users for selected language
                                chatViewModel.loadRandomUserBatch(
                                    currentUserId = user.uid,
                                    userRepository = userViewModel.repository,
                                    onNotEnoughSwipes = {
                                        Toast.makeText(context, "No swipes left today.", Toast.LENGTH_SHORT).show()
                                    },
                                    onComplete = { success ->
                                        if (!success) {
                                            Toast.makeText(context, "No users found in selected language.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            })
                            {
                                Text(lang.uppercase())
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNextUser,
                    modifier = Modifier.size(56.dp) // 👈 larger tap area
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next user",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp) // 👈 larger arrow
                    )
                }
            }
        }

    }
}


