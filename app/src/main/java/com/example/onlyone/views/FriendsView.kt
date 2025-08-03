package com.example.onlyone.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.composables.BlinkingIcon
import com.example.onlyone.composables.FriendItem
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.utils.toPublicUser
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.UserViewModel

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

    val tabTitles = listOf("Friends", "Incoming", "Outgoing", "Blocked")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // 🔹 Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Connections", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        userViewModel.loadUser() // 🔄 Re-fetch user data
                        Toast.makeText(context, "Refreshing connections...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Connections"
                    )
                }
            }

            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Friend"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, baseTitle ->
                val label: @Composable () -> Unit = {
                    when (baseTitle) {
                        "Incoming" -> BlinkingIcon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Incoming",
                            shouldBlink = incomingCount > 0
                        )
                        "Outgoing" -> Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_forward_24),
                            contentDescription = "Outgoing"
                        )
                        "Blocked" -> Icon(
                            painter = painterResource(id = R.drawable.baseline_block_24),
                            contentDescription = "Blocked"
                        )
                        else -> Text("Friends")
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
               // Log.d("FriendsView", "Rendering Friends tab with ${localFriends.size} friends")

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    items(localFriends) { friend ->
                        //Log.d("FriendsView", "Rendering friend: ${friend.username}, uid=${friend.uid}")

                        FriendItem(
                            avatarResId = mapAvatarIdToDrawable(friend.avatarId),
                            name = friend.username,
                            status = friend.moodStatus,
                            isLocked = friend.uid in writtenIds,
                            onWriteClick = {
                                //Log.d("FriendsView", "Write clicked for ${friend.username} (${friend.uid})")
                                if (friend.uid !in writtenIds) {
                                    userViewModel.setTargetUser(friend.toPublicUser())
                                    val route = "ChatScreen/${friend.uid}?isRandom=false"
                                    Log.d("FriendsView", "friend.uid: ${friend.uid}")
                                    navController.navigate(route)
                                } else {
                                    Log.d("FriendsView", "User already written to today")
                                }
                            },
                            showDelete = true,
                            onDelete = { userViewModel.deleteFriend(friend.uid) },
                            onBlock = { userViewModel.blockUser(friend.uid) } // ✅ Add this line
                        )
                    }
                }
            }
            1 -> {
                // 🔹 Incoming Requests
                val incomingUids = user?.incomingFriendRequests ?: emptyList()
                if (incomingUids.isEmpty()) {
                    Text("No incoming friend requests.", modifier = Modifier.padding(12.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(incomingUids) { uid ->
                            val username = incomingRequests[uid] ?: "Unknown"
                            FriendItem(
                                name = username,
                                status = "Wants to connect",
                                avatarResId = mapAvatarIdToDrawable(0),
                                showAccept = true,
                                showDecline = true,
                                onAccept = { userViewModel.acceptFriendRequest(uid) },
                                onDecline = { userViewModel.declineFriendRequest(uid) },
                                onBlock = { userViewModel.blockUser(uid) } // ✅ Add block support
                            )
                        }
                    }
                }
            }

            2 -> {
                // 🔹 Outgoing Requests
                val outgoingUids = user?.outgoingFriendRequests ?: emptyList()
                if (outgoingUids.isEmpty()) {
                    Text("No outgoing requests.", modifier = Modifier.padding(12.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(outgoingUids) { uid ->
                            val username = outgoingUsernames[uid] ?: "Pending"

                            FriendItem(
                                name = username,
                                status = "Request sent",
                                avatarResId = mapAvatarIdToDrawable(0),
                                showDecline = true,
                                onDecline = {
                                    userViewModel.cancelOutgoingFriendRequest(
                                        targetUid = uid,
                                        onSuccess = {
                                            Toast.makeText(context, "Request canceled", Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Failed to cancel request", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
            3 -> {
                val blockedUsers = userViewModel.blockedUsers.collectAsState(initial = emptyList()).value

                if (blockedUsers.isEmpty()) {
                    Text("No blocked users.", modifier = Modifier.padding(12.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(blockedUsers) { user ->
                            FriendItem(
                                avatarResId = mapAvatarIdToDrawable(user.avatarId),
                                name = user.username,
                                status = "Blocked",
                                isLocked = true,
                                onUnblock = { userViewModel.unblockUser(user.uid) },
                                onUnblockAndRequest = {
                                    userViewModel.unblockUser(user.uid)
                                    userViewModel.sendFriendRequestDirect(user.uid)
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
            title = { Text("Send Friend Request") },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Friend's Email") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    userViewModel.sendFriendRequestByEmail(
                        email = email,
                        onSuccess = {
                            Toast.makeText(context, "Friend request sent!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        },
                        onFailure = { reason ->
                            Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



