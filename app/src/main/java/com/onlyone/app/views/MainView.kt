package com.onlyone.app.views

import CustomAlertDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.onlyone.app.Screen
import com.onlyone.app.data.LocalMessage
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.formatTimeLeft
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.SessionViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.composables.ReceivedMessageItem
import com.onlyone.app.composables.ReceivedMessageItemBig
import com.onlyone.app.composables.SavedMessageItem
import com.onlyone.app.composables.UserStatsCardContent

@Composable
fun MainView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
    sessionViewModel: SessionViewModel,
    theme: ThemeTokens
) {
    // ─────────────────────────────────────────────────────────────────────────────
    // System UI colors (run on theme change, not every recomposition)
    // ─────────────────────────────────────────────────────────────────────────────
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(theme) {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = true)
        systemUiController.setNavigationBarColor(color = Color.Transparent, darkIcons = false)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Observed user & stable flows (avoid re-creating listeners on recomposition)
    // ─────────────────────────────────────────────────────────────────────────────
    val user by userViewModel.user.observeAsState()

    val uid = user?.uid.orEmpty()

    // Stabilize VM-provided flows so we don't rebuild them on recompositions
    val localMsgsFlow = remember(uid) { chatViewModel.observeLocalMessages(uid) }
    val localMessages by localMsgsFlow.collectAsState(initial = emptyList())

    val favoriteFlow = remember { chatViewModel.observeFavoriteMessages() }
    val favoriteMessages by favoriteFlow.collectAsState(initial = emptyList())

    // ─────────────────────────────────────────────────────────────────────────────
    // Local UI state
    // ─────────────────────────────────────────────────────────────────────────────
    var selectedMessage by remember { mutableStateOf<LocalMessage?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    var showReportsDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Favourite optimistic UI state
    val currentFavId = user?.favouriteMessage?.messageId
    var busyFavId by remember { mutableStateOf<String?>(null) }
    var pendingFavId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentFavId) { pendingFavId = null }
    val effectiveFavId = pendingFavId ?: currentFavId

    // ─────────────────────────────────────────────────────────────────────────────
    // One-off effects
    // ─────────────────────────────────────────────────────────────────────────────
    // Check admin access once per composable instance
    LaunchedEffect(Unit) { chatViewModel.checkAdminAccess() }
    val isAdmin by chatViewModel.isAdmin.collectAsState()

    // Initial sync when user becomes available (guarded in VM)
    LaunchedEffect(user?.uid) {
        val id = user?.uid ?: return@LaunchedEffect
        if (userViewModel.shouldLoadMessagesFor(id)) {
            isSyncing = true
            syncFailed = !chatViewModel.syncMessagesFromServer(id)
            isSyncing = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp, start = 4.dp, end = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: quick actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //SmallResetButton("WM") { chatViewModel.hardResetWritten() }
                //TestVideoAdButton()

                if (isAdmin) {
                    OutlinedButton(
                        onClick = {
                            showReportsDialog = true
                            chatViewModel.loadReports("open", 50)
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Moderation", fontSize = 12.sp)
                    }
                }
            }

            // Stats / Profile section (inside overlay for theme look)
            CustomColorOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                theme = theme,
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
                    onAvatarSelected = { newAvatarId ->
                        userViewModel.updatePublicProfile(
                            updates = mapOf("avatarId" to newAvatarId),
                            onSuccess = {},
                            onFailure = {}
                        )
                    },
                    onFeedbackClick = {
                        navController.navigate(Screen.FeedbackScreen.route)
                    },
                    onLogoutClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxSize(),
                    theme = theme,
                    navController = navController
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs: Received / Saved
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
                    contentColor = theme.textColor,
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
                            text = { Text(label, color = theme.textColor) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                when (selectedTab) {
                    // ── RECEIVED ────────────────────────────────────────────────
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
                                                color = theme.textColor,
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
                                                color = theme.textColor,
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
                                            feedback = message.feedback ?: -10,
                                            theme = theme
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── SAVED ───────────────────────────────────────────────────
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (favoriteMessages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.main_no_messages),
                                            color = theme.textColor,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
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
                                                fav = fav,
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
                                        onClick = { /* optional */ },
                                        theme = theme
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Dialogs / Overlays
    // ─────────────────────────────────────────────────────────────────────────────

    // Logout dialog
    if (showLogoutDialog) {
        CustomAlertDialog(
            theme = theme,
            borderColor = theme.borderColor,
            onDismiss = { showLogoutDialog = false }
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.main_logout_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.main_logout_text),
                    style = MaterialTheme.typography.body1,
                    color = theme.textColor
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(R.string.common_no), color = theme.textColor)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        sessionViewModel.signOut()
                        showLogoutDialog = false
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo("MainScreen") { inclusive = true }
                        }
                    }) {
                        Text(stringResource(R.string.common_yes), color = theme.textColor)
                    }
                }
            }
        }
    }

    // Admin moderation dialog
    if (showReportsDialog) {
        AdminReportsDialog(
            chatViewModel = chatViewModel,
            onDismiss = { showReportsDialog = false },
            theme = theme
        )
    }

    // Fullscreen message preview (Received)
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
                shape = 20,
                theme = theme
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
