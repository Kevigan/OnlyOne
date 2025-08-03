package com.example.onlyone.composables

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val targetUser by userViewModel.targetUser.collectAsState()
    val isLoading by userViewModel.isLoadingUserBatch.collectAsState()
    val context = LocalContext.current

    // ✅ Show toast messages from ViewModel
    LaunchedEffect(Unit) {
        userViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRandom, uid, currentUser?.uid) {
        if (currentUser == null) return@LaunchedEffect

        if (isRandom) {
            userViewModel.loadRandomUserBatchIfNeeded(showToasts = true)
        } else {
            // ✅ Load targetUser from Firestore if not already set
            if (userViewModel.targetUser.value == null || userViewModel.targetUser.value?.uid != uid) {
                userViewModel.loadFriendById(uid)
            }
        }
    }

    Log.d("ChatScreenNav", "📦 currentUser= $currentUser")
    Log.d("ChatScreenNav", "📦 isRandom: , $isRandom")
    Log.d("ChatScreenNav", "📦 targetUser: , $targetUser")

    // ✅ Always show ChatView if in random mode and user is available
    if (currentUser != null && (isRandom || targetUser != null)) {
        ChatView(
            user = currentUser!!,
            targetUser = targetUser, // can be null
            isRandom = true,
            onNextUser = { userViewModel.consumeNextUserFromQueue() },
            chatViewModel = chatViewModel,
            userViewModel = userViewModel,
            navController = navController
        )
    }
}


