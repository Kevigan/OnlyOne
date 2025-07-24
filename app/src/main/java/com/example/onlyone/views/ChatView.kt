package com.example.onlyone.views

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
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.Message
import com.example.onlyone.data.User
import com.example.onlyone.repos.UserRepository
import com.example.onlyone.viewModels.ChatViewModel
import com.google.firebase.Timestamp

@Composable
fun ChatView(
    user: User,
    targetUser: PublicUser,
    isRandom: Boolean,
    onNextUser: () -> Unit,
    chatViewModel: ChatViewModel,
    userRepository: UserRepository
) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current


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
            onValueChange = { messageText = it },
            label = { Text("Write your message") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Send + (optional) Next button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val msg = Message(
                        senderUsername = user.username,
                        senderId = user.uid,
                        receiverId = targetUser.uid,
                        content = messageText.trim(),
                        timestamp = Timestamp.now(),
                        senderAvatarId = 0,
                        feedback = -10
                    )
                    chatViewModel.sendMessage(msg) { success ->
                        if (success) {
                            messageText = ""
                        }
                        // Optional: Show toast or error
                    }
                },
                enabled = messageText.isNotBlank()
            ) {
                Text("Send")
            }

            // 🔄 Show next only if isRandom
            if (isRandom) {
                IconButton(onClick = onNextUser) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next user",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


