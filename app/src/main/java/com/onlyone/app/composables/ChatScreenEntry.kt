package com.onlyone.app.composables

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.toPublicUser
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.onlyone.app.views.chat.ChatView

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ChatScreenEntry(
    uid: String,
    isRandom: Boolean,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
    theme: ThemeTokens
) {
    val currentUser by userViewModel.user.observeAsState()
    val targetUser by userViewModel.targetUser.collectAsState()
    val localFriends by userViewModel.observeLocalFriends().collectAsState(initial = emptyList())
    val context = LocalContext.current

    // Show ViewModel toasts
    LaunchedEffect(Unit) {
        userViewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Random mode: make sure there’s a batch ready
    LaunchedEffect(isRandom, currentUser?.uid) {
        if (isRandom && currentUser != null) {
            userViewModel.loadRandomUserBatchIfNeeded(showToasts = true)
        }
    }

    // Friend mode: rehydrate targetUser from local cache by uid (no network)
    LaunchedEffect(isRandom, uid, localFriends) {
        if (!isRandom && (targetUser?.uid != uid)) {
            localFriends.firstOrNull { it.uid == uid }?.toPublicUser()?.let {
                userViewModel.setTargetUser(it)
            }
        }
    }

    // ---- Rendering guards (never draw nothing) ----
    if (currentUser == null) {
        FullscreenStatus(theme, "Loading your profile…")
        return
    }

    if (isRandom) {
        // ChatView already shows the “searching” ghost when targetUser == null
        ChatView(
            user = currentUser!!,
            targetUser = targetUser,
            isRandom = true,
            onNextUser = { userViewModel.consumeNextUserFromQueue() },
            chatViewModel = chatViewModel,
            userViewModel = userViewModel,
            navController = navController,
            theme = theme
        )
        return
    }

    // Friend mode
    when {
        targetUser?.uid == uid -> ChatView(
            user = currentUser!!,
            targetUser = targetUser,
            isRandom = false,
            onNextUser = {},
            chatViewModel = chatViewModel,
            userViewModel = userViewModel,
            navController = navController,
            theme = theme
        )

        // We have the friend locally, LaunchedEffect will setTargetUser in a tick
        localFriends.any { it.uid == uid } -> FullscreenStatus(theme, stringResource(R.string.chat_status_opening))

        // Not in local storage (deleted/blocked/stale deep link)
        else -> FullscreenStatus(theme, stringResource(R.string.chat_status_unavailable))
    }
}

@Composable
private fun FullscreenStatus(theme: ThemeTokens, text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = theme.textColor.copy(alpha = 0.9f))
            Spacer(Modifier.height(16.dp))
            Text(text, color = theme.textColor)
        }
    }
}


