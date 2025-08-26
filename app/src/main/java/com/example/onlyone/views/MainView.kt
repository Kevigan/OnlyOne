package com.example.onlyone.views

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.composables.*
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.utils.DailyResetTimer
import com.example.onlyone.utils.formatTimeLeft
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun MainView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
    sessionViewModel: SessionViewModel
) {
    val systemUiController = rememberSystemUiController()
    val user by userViewModel.user.observeAsState()

    // 24h (received) messages
    val localMessages by chatViewModel.observeLocalMessages(user?.uid.orEmpty())
        .collectAsState(initial = emptyList())

    // Saved favorites (VM exposes no-arg observer)
    val favoriteMessages by chatViewModel.observeFavoriteMessages()
        .collectAsState(initial = emptyList())

    val millisUntilReset by DailyResetTimer.timeUntilReset.collectAsState()

    var selectedMessage by remember { mutableStateOf<LocalMessage?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentFavId = user?.favouriteMessage?.messageId

    // Local UI state to prevent double taps and show immediate selection
    var busyFavId by remember { mutableStateOf<String?>(null) }
    var pendingFavId by remember { mutableStateOf<String?>(null) }

    // When the backend/userViewModel pushes the new favourite to `user`,
    // clear the pending state so the real value drives the UI.
    LaunchedEffect(currentFavId) { pendingFavId = null }

    // Use either the pending value (optimistic) or the current one from user
    val effectiveFavId = pendingFavId ?: currentFavId

    // Sync on app start / user change
    LaunchedEffect(user?.uid) {
        val uid = user?.uid
        if (uid != null && userViewModel.shouldLoadMessagesFor(uid)) {
            isSyncing = true
            syncFailed = !chatViewModel.syncMessagesFromServer(uid)
            isSyncing = false
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = true)
        systemUiController.setNavigationBarColor(color = Color.Transparent, darkIcons = false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp, start = 4.dp, end = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // === Header row ===
            /*CustomColorOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                paddingBox1 = PaddingValues(6.dp),
                paddingBox2 = PaddingValues(1.dp),
                borderWidth = 1.dp,
                gradientColor1 = Color(0xFF001F54).copy(alpha = 0.95f),
                gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                onDismiss = {}
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayName = user?.username ?: stringResource(R.string.common_user)
                    Text(
                        text = stringResource(R.string.main_greeting, displayName),
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallResetButton("Reset FL") { userViewModel.hardResetFriends() }
                        SmallResetButton("Reset LM") { userViewModel.hardResetLocalMessages() }

                        IconButton(onClick = { showLogoutDialog = true }) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_logout_24),
                                contentDescription = stringResource(R.string.main_cd_logout),
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }
                }
            }*/

            Spacer(modifier = Modifier.height(8.dp))

            // Compute your mood icon exactly like before
            val moodResId = user?.moodId?.let {
                if (it != 0) mapMoodIdToDrawable(it) else R.drawable.baseline_tag_faces_24
            } ?: R.drawable.baseline_tag_faces_24

            CustomColorOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                onDismiss = {}
            ) {
                UserStatsCardContent(
                    user = user,
                    onMoodSubmit = { newMood ->
                        userViewModel.updatePublicProfile(
                            updates = mapOf("moodStatus" to newMood),
                            onSuccess = {},
                            onFailure = {}
                        )
                    },
                    onMoodIconSelected = { newMoodId ->
                        userViewModel.updatePublicProfile(
                            updates = mapOf("moodId" to newMoodId),
                            onSuccess = {},
                            onFailure = {}
                        )
                    },
                    onAvatarSelected = { newAvatarId ->          // NEW
                        userViewModel.updatePublicProfile(
                            updates = mapOf("avatarId" to newAvatarId),
                            onSuccess = {},
                            onFailure = {}
                        )
                    },
                    onLogoutClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            // ===== Tabs (NO overlay here) =====
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf(
                stringResource(R.string.main_tab_received),
                stringResource(R.string.main_tab_saved)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(2.dp)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(label, color = Color.White) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                when (selectedTab) {
                    // --- RECEIVED tab ---
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                                text = stringResource(R.string.main_error_loading_messages),
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
                                            Text(
                                                stringResource(R.string.main_no_messages),
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
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

                    // --- SAVED tab ---
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (favoriteMessages.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(text = "No saved messages yet.", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                // Saved tab content
                                items(favoriteMessages) { fav ->
                                    val isThisSelected = (effectiveFavId == fav.id)
                                    val isProcessing = (busyFavId == fav.id)

                                    SavedMessageItem(
                                        avatarResId = R.drawable.baseline_tag_faces_24,
                                        name = fav.senderUsername,
                                        message = fav.content,
                                        isFavorite = isThisSelected,
                                        isProcessing = isProcessing,
                                        onStarClick = {
                                            if (busyFavId != null) return@SavedMessageItem
                                            val wasSelected = isThisSelected
                                            busyFavId = fav.id
                                            pendingFavId = if (wasSelected) null else fav.id
                                            userViewModel.toggleFavouriteMessage(
                                                messageId = fav.id,
                                                onSuccess = {
                                                    busyFavId = null
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(
                                                            if (wasSelected) R.string.toast_favourite_cleared
                                                            else R.string.toast_favourite_set
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onFailure = {
                                                    pendingFavId = null
                                                    busyFavId = null
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.toast_favourite_update_failed),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        },
                                        onDeleteConfirm = {
                                            chatViewModel.removeFavorite(fav.id)
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_removed_from_saved),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onClick = { /* optional */ }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.main_logout_title)) },
            text = { Text(stringResource(R.string.main_logout_text)) },
            confirmButton = {
                TextButton(onClick = {
                    sessionViewModel.signOut()
                    showLogoutDialog = false
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo("MainScreen") { inclusive = true }
                    }
                }) { Text(stringResource(R.string.common_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_no))
                }
            }
        )
    }

    // Fullscreen message preview (only for Received)
    if (selectedMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable(onClick = { selectedMessage = null })
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
        Text(label, fontSize = 12.sp)
    }
}
