package com.example.onlyone.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.ReceivedMessageItem
import com.example.onlyone.composables.UserStatsCardContent
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dao.FriendDao
import com.example.onlyone.cloudMessaging.MessageNotifier
import com.example.onlyone.cloudMessaging.RequestNotificationPermission
import com.example.onlyone.composables.MoodStatusCardContent
import com.example.onlyone.composables.ReceivedMessageItemBig
import com.example.onlyone.composables.TopSnackbar
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.utils.DailyResetTimer
import com.example.onlyone.utils.formatTimeLeft
import kotlinx.coroutines.delay

@Composable
fun MainView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
    sessionViewModel: SessionViewModel
) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    val user by userViewModel.user.observeAsState()
    val localMessages by chatViewModel.observeLocalMessages(user?.uid.orEmpty())
        .collectAsState(initial = emptyList())
    val millisUntilReset by DailyResetTimer.timeUntilReset.collectAsState()
    val statusBarColor = MaterialTheme.colors.background

    var selectedMessage by remember { mutableStateOf<LocalMessage?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }

   /* LaunchedEffect(user?.uid) {
        user?.uid?.let { userViewModel.checkAndResetSwipeLimit() }
    }*/
//for app start
    LaunchedEffect(user?.uid) {
        val uid = user?.uid
        if (uid != null && userViewModel.shouldLoadMessagesFor(uid)) {
            isSyncing = true
            syncFailed = false
            val success = chatViewModel.syncMessagesFromServer(uid)
            isSyncing = false
            syncFailed = !success
        }
    }

    LaunchedEffect(user?.uid) {//for when new message reveived
        val uid = user?.uid
        if (uid != null) {
            chatViewModel.messageFlow.collect { (_, _) ->
                chatViewModel.syncMessagesFromServer(uid)
            }
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(color = statusBarColor, darkIcons = true)
        systemUiController.setNavigationBarColor(color = Color.Transparent, darkIcons = false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🟣 1/8 — Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hello ${user?.username ?: "User"}",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallResetButton("WM") { chatViewModel.hardResetWritten() }
                    SmallResetButton("F") { userViewModel.hardResetFriends() }
                    SmallResetButton("LM") { userViewModel.hardResetLocalMessages() }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }
            }
            CustomColorOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                paddingBox1 = PaddingValues(6.dp),
                paddingBox2 = PaddingValues(1.dp),
                shape = RoundedCornerShape(24.dp),
                onDismiss = {}
            ) {
                MoodStatusCardContent(
                    moodStatus = user?.moodStatus.orEmpty(),
                    avatarResId = R.drawable.baseline_tag_faces_24,
                    onMoodSubmit = { newMood -> userViewModel.updateMood(newMood) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 🟣 2/8 — Overlay
            CustomColorOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                onDismiss = {}
            ) {
                UserStatsCardContent(
                    messagesLeft =25, //swipeStatus?.let { it.swipesGranted - it.swipesUsed } ?: 0,
                    points = (user?.points ?: 0).toString(),
                    pointsRank = user?.points ?: 0,
                    gold = user?.gold ?: 0,
                    runesRare = user?.runes_rare ?: 0,
                    runesSuperRare = user?.runes_super_rare ?: 0,
                    runesMegaRare = user?.runes_mega_rare ?: 0,
                    millisUntilReset = millisUntilReset
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🟣 5/8 — Message list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            "Received Messages",
                            style = MaterialTheme.typography.h6,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    when {
                        isSyncing -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        syncFailed -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚠️ Couldn't load messages. Check your connection.",
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        localMessages.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No messages yet.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }

                        else -> {
                            items(localMessages) { message ->
                                ReceivedMessageItem(
                                    avatarResId = R.drawable.baseline_tag_faces_24,
                                    name = message.senderUsername,
                                    message = message.content,
                                    expiration = formatTimeLeft(message.timestamp),
                                    onClick = { selectedMessage = message },
                                    isRead = message.read,
                                    feedback = message.feedback ?: -10
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Do you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    sessionViewModel.signOut()
                    showLogoutDialog = false
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo("MainScreen") { inclusive = true }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (selectedMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000)) // semi-transparent backdrop
                .clickable(onClick = { selectedMessage = null }) // dismiss on outside tap
        ) {
            ReceivedMessageItemBig(
                chatViewModel = chatViewModel,
                userViewModel = userViewModel,
                message = selectedMessage!!,
                onFeedbackSelected = { feedback ->
                    chatViewModel.addFeedback(selectedMessage!!, feedback)
                    selectedMessage = null
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = 20
            )
        }
    }
}

@Composable
fun SmallResetButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors()
    ) {
        Text("🔄 $label", fontSize = 12.sp)
    }
}
