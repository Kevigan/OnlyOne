package com.example.onlyone.views

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.FriendItem
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.viewModels.UserViewModel

@Composable
fun FriendsView(userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()
    val incomingRequests by userViewModel.incomingRequestUsernames.collectAsState()
    val friends by userViewModel.friends.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        // 🔹 Top bar with action icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Friends", style = MaterialTheme.typography.h5)

            Row {
                IconButton(onClick = { showRequestsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Friend Requests",
                        tint = if (incomingRequests.isNotEmpty()) Color.Red else Color.Gray
                    )
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Friend"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Display Incoming Friend Requests
        if (incomingRequests.isNotEmpty()) {
            Text("Incoming Friend Requests:", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                items(incomingRequests) { username ->
                    // Display the username for each incoming request
                    Text("Friend Request from $username")
                    // Add buttons or interactions for accepting/rejecting the request
                }
            }
        }

        // 🔹 Friend list (replace with real loaded users)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            items(friends) { friend ->
                FriendItem(
                    name = friend.username,
                    status = friend.moodStatus,
                    avatarResId = mapAvatarIdToDrawable(friend.avatarId)
                )
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
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Friend's Email") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    userViewModel.sendFriendRequestByEmail(email)
                    showAddDialog = false
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

    // 🔹 Incoming Requests Dialog
    if (showRequestsDialog) {
        AlertDialog(
            onDismissRequest = { showRequestsDialog = false },
            title = { Text("Incoming Friend Requests") },
            text = {
                Column {
                    user?.incomingFriendRequests?.forEachIndexed { index, uid ->
                        // Check if the username exists at the current index of incomingRequests
                        val username = incomingRequests.getOrNull(index) ?: "Unknown"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Username: $username") // Show username instead of UID
                            TextButton(onClick = {
                                showRequestsDialog = false
                                userViewModel.acceptFriendRequest(uid)
                            }) {
                                Text("Accept")
                            }
                        }
                    }
                    if (user?.incomingFriendRequests.isNullOrEmpty()) {
                        Text("No incoming requests.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRequestsDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {}
        )
    }
}


