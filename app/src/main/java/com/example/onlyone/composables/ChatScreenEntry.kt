package com.example.onlyone.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.example.onlyone.views.ChatView

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ChatScreenEntry(
    uid: String,
    isRandom: Boolean,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    val currentUser by userViewModel.user.observeAsState()
    val targetUser by chatViewModel.targetUser.collectAsState()
    val loadingUsers by chatViewModel.isLoadingUser.collectAsState()

    LaunchedEffect(isRandom, uid, currentUser?.uid) {
        if (currentUser == null) return@LaunchedEffect

        val currentUid = currentUser!!.uid
        Log.d("ChatScreen", "LaunchedEffect → isRandom=$isRandom, uid=$uid")

        if (isRandom) {
            if (chatViewModel.userQueue.value.isEmpty()) {
                Log.d("ChatScreen", "→ Loading random user batch")
                chatViewModel.loadRandomUserBatch(
                    currentUserId = currentUid,
                    userRepository = userViewModel.repository,
                    onNotEnoughSwipes = {
                        Log.w("ChatScreen", "⚠️ Not enough swipes")
                    },
                    onComplete = { success ->
                        Log.d("ChatScreen", "✅ Batch loaded: success=$success")
                    }
                )
            }
        } else {
            Log.d("ChatScreen", "→ Loading target user uid=$uid")
            chatViewModel.loadTargetUser(uid, isRandom = false, userViewModel)
        }
    }

    Log.d("ChatScreen", "⏳ Waiting: currentUser=${currentUser?.uid}, targetUser=${targetUser?.uid}")

    when {
        currentUser != null && targetUser != null -> {
            ChatView(
                user = currentUser!!,
                targetUser = targetUser!!,
                isRandom = isRandom,
                onNextUser = { chatViewModel.consumeNextUserFromQueue(currentUser!!.uid) },
                chatViewModel = chatViewModel,
                userViewModel = userViewModel,
                navController = navController
            )
        }

        currentUser != null && isRandom && loadingUsers -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        currentUser != null && isRandom && chatViewModel.userQueue.value.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉 You've seen everyone for now!", style = MaterialTheme.typography.h2)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { /* optionally handle navigation */ }) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}
