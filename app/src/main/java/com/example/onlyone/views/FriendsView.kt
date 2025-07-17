package com.example.onlyone.views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.FriendItem
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.viewModels.UserViewModel

@Composable
fun FriendsView(userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()
    val incomingRequests by userViewModel.incomingRequestUsernames.collectAsState()
    val outgoingUsernames by userViewModel.outgoingRequestUsernames.collectAsState()
    val incomingCount = incomingRequests.size
    val outgoingCount = outgoingUsernames.size
    val friends by userViewModel.friends.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabTitles = listOf("Friends", "Incoming", "Outgoing")

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
            Text("Connections", style = MaterialTheme.typography.h5)

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
                val label = when (baseTitle) {
                    "Incoming" -> if (incomingCount > 0) "Incoming ($incomingCount)" else "Incoming"
                    "Outgoing" -> if (outgoingCount > 0) "Outgoing ($outgoingCount)" else "Outgoing"
                    else -> baseTitle
                }

                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tab content
        when (selectedTabIndex) {
            0 -> {
                // 🔹 Friends
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    items(friends) { friend ->
                        FriendItem(
                            name = friend.username,
                            status = friend.moodStatus,
                            avatarResId = mapAvatarIdToDrawable(friend.avatarId),
                            canWrite = false,
                            onWriteClick = {}
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
                        itemsIndexed(incomingUids) { index, uid ->
                            val username = incomingRequests.getOrNull(index) ?: "Unknown"
                            FriendItem(
                                name = username,
                                status = "Wants to connect",
                                avatarResId = mapAvatarIdToDrawable(0), // You could enhance with avatar lookup
                                showAccept = true,
                                showDecline = true,
                                onAccept = { userViewModel.acceptFriendRequest(uid) },
                                onDecline = { userViewModel.declineFriendRequest(uid) }
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
                        itemsIndexed(outgoingUids) { index, uid ->
                            val username = outgoingUsernames.getOrNull(index) ?: "Pending"

                            FriendItem(
                                name = username,
                                status = "Request sent",
                                avatarResId = mapAvatarIdToDrawable(0),
                                showDecline = true,
                                onDecline = {
                                    userViewModel.cancelOutgoingFriendRequest(uid,
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



