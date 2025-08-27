package com.example.onlyone.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.composables.BlinkingIcon
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.FriendItem
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.utils.toPublicUser
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun FriendsView(
    userViewModel: UserViewModel,
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    val user by userViewModel.user.observeAsState()
    val incomingRequests by userViewModel.incomingRequestUsernames.collectAsState()
    val outgoingUsernames by userViewModel.outgoingRequestUsernames.collectAsState()
    val incomingCount = incomingRequests.size
    val outgoingCount = outgoingUsernames.size
    val localFriends by userViewModel.observeLocalFriends().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val writtenList by chatViewModel.writtenTodayList.collectAsState(initial = emptyList())
    val writtenIds = writtenList.map { it.receiverId }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var expandedUid by remember { mutableStateOf<String?>(null) }

    // Localized tab titles (used only for "Friends" text tab)
    val tabTitles = listOf(
        stringResource(R.string.friends_tab_friends),
        stringResource(R.string.friends_tab_incoming),
        stringResource(R.string.friends_tab_outgoing),
        stringResource(R.string.friends_tab_blocked)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // 🔹 Top bar
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth(),
            paddingBox1 = PaddingValues(6.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 =  Color(0xFF001F54).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            onDismiss = {}
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.friends_header),
                        style = MaterialTheme.typography.h5,
                        color = Color.White // ⬅ text color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            userViewModel.loadUser()
                            Toast.makeText(
                                context,
                                context.getString(R.string.friends_refreshing),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.friends_cd_reload),
                            tint = Color.White // ⬅ icon color
                        )
                    }
                }

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = stringResource(R.string.friends_cd_add),
                        tint = Color.White // ⬅ icon color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            (0..3).forEach { index ->
                val label: @Composable () -> Unit = {
                    when (index) {
                        1 -> BlinkingIcon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.friends_cd_incoming),
                            shouldBlink = incomingCount > 0
                        )

                        2 -> Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_forward_24),
                            contentDescription = stringResource(R.string.friends_cd_outgoing)
                        )

                        3 -> Icon(
                            painter = painterResource(id = R.drawable.baseline_block_24),
                            contentDescription = stringResource(R.string.friends_cd_blocked)
                        )

                        else -> Text(tabTitles[0]) // "Friends"
                    }
                }
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = label
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tab content
        when (selectedTabIndex) {
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    items(localFriends, key = { it.uid }) { friend ->
                        FriendItem(
                            avatarResId = mapAvatarIdToDrawable(friend.avatarId),
                            name = friend.username,
                            status = friend.moodStatus,
                            isLocked = friend.uid in writtenIds,
                            expanded = expandedUid == friend.uid,
                            onCardClick = { expandedUid = if (expandedUid == friend.uid) null else friend.uid },
                            onWriteClick = {
                                if (friend.uid !in writtenIds) {
                                    userViewModel.setTargetUser(friend.toPublicUser())
                                    navController.navigate(
                                        Screen.ChatScreen.createRoute(friend.uid, false)
                                    )
                                }
                            },
                            showDelete = true,
                            onDelete = { userViewModel.deleteFriend(friend.uid) },
                            onBlock = { userViewModel.blockUser(friend.uid) },

                            // NEW:
                            favouriteMessage  = friend.favouriteMessage,
                            achievementCount = friend.achievementCount
                        )
                    }
                }
            }
            1 -> {
                // 🔹 Incoming Requests
                val incomingUids = user?.incomingFriendRequests ?: emptyList()
                if (incomingUids.isEmpty()) {
                    Text(
                        stringResource(R.string.friends_empty_incoming),
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(incomingUids) { uid ->
                            val username =
                                incomingRequests[uid] ?: stringResource(R.string.friends_unknown)
                            FriendItem(
                                name = username,
                                status = stringResource(R.string.friends_status_wants_connect),
                                avatarResId = mapAvatarIdToDrawable(0),
                                showAccept = true,
                                showDecline = true,
                                onAccept = { userViewModel.acceptFriendRequest(uid) },
                                onDecline = { userViewModel.declineFriendRequest(uid) },
                                onBlock = { userViewModel.blockUser(uid) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // 🔹 Outgoing Requests
                val outgoingUids = user?.outgoingFriendRequests ?: emptyList()
                if (outgoingUids.isEmpty()) {
                    Text(
                        stringResource(R.string.friends_empty_outgoing),
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(outgoingUids) { uid ->
                            val username =
                                outgoingUsernames[uid] ?: stringResource(R.string.friends_pending)
                            FriendItem(
                                name = username,
                                status = stringResource(R.string.friends_status_request_sent),
                                avatarResId = mapAvatarIdToDrawable(0),
                                showDecline = true,
                                onDecline = {
                                    userViewModel.cancelOutgoingFriendRequest(
                                        targetUid = uid,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.friends_toast_request_canceled),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.friends_toast_cancel_failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            3 -> {
                val blockedUsers =
                    userViewModel.blockedUsers.collectAsState(initial = emptyList()).value

                if (blockedUsers.isEmpty()) {
                    Text(
                        stringResource(R.string.friends_empty_blocked),
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(blockedUsers) { blocked ->
                            FriendItem(
                                avatarResId = mapAvatarIdToDrawable(blocked.avatarId),
                                name = blocked.username,
                                status = stringResource(R.string.friends_status_blocked),
                                isLocked = true,
                                onUnblock = { userViewModel.unblockUser(blocked.uid) },
                                onUnblockAndRequest = {
                                    userViewModel.unblockUser(blocked.uid)
                                    userViewModel.sendFriendRequestDirect(blocked.uid)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🔹 Add Friend Dialog
    if (showAddDialog) {
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.friends_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.friends_dialog_email_label)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.friends_toast_email_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TextButton
                    }

                    userViewModel.sendFriendRequestByEmail(
                        email = email,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.friends_toast_request_sent),
                                Toast.LENGTH_SHORT
                            ).show()
                            showAddDialog = false
                        },
                        onFailure = { reason ->
                            Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text(stringResource(R.string.friends_dialog_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.friends_dialog_cancel))
                }
            }
        )
    }
}
