package com.onlyone.app.views

import CustomAlertDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.Screen
import com.onlyone.app.R
import com.onlyone.app.composables.BlinkingIcon
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.composables.FriendItem
import com.onlyone.app.composables.mapAvatarIdToDrawable
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.toPublicUser
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel

@Composable
fun FriendsView(
    userViewModel: UserViewModel,
    navController: NavController,
    chatViewModel: ChatViewModel,
    theme: ThemeTokens
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

    val friendCount = localFriends.size
    val maxFriends = 20

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
            theme = theme,
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
                        color = theme.textColor// ⬅ text color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(
                                color = theme.textColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.friends_header_count, friendCount, maxFriends),
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor
                        )
                    }
                }

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = stringResource(R.string.friends_cd_add),
                        tint = theme.textColor // ⬅ icon color
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tabs
        val tabsBg = Brush.horizontalGradient(
            listOf(
                theme.gradientColor1.copy(alpha = 0.9f),
                theme.gradientColor2.copy(alpha = 0.9f)
                // or just repeat the same color twice if you want a flat tint
                // theme.cardBackground.copy(alpha = 0.9f), theme.cardBackground.copy(alpha = 0.9f)
            )
        )

        Box(
            Modifier
                .fillMaxWidth()
                .background(tabsBg, shape = RoundedCornerShape(24.dp))
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                backgroundColor = Color.Transparent,     // <-- important
                contentColor = theme.cardContentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = theme.textColor
                    )
                },
                divider = {} // optional: remove bottom divider
            ) {
                (0..3).forEach { index ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        selectedContentColor   = theme.textColor,
                        unselectedContentColor = theme.textColor.copy(alpha = 0.6f),
                        // Inside your TabRow -> Tab(...) loop
                        icon = {
                            when (index) {
                                // Incoming
                                1 -> {
                                    Box(modifier = Modifier.size(24.dp)) {
                                        BlinkingIcon(
                                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                                            contentDescription = stringResource(R.string.friends_cd_incoming),
                                            shouldBlink = incomingCount > 0,
                                            baseColor = theme.textColor,
                                            //modifier = Modifier.matchParentSize()
                                        )
                                        if (incomingCount > 0) {
                                            // half on / half off the icon (top-right)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-6).dp, y = (-6).dp)
                                            ) { PlusBadgeTiny() }
                                        }
                                    }
                                }
                                2 -> Icon(painterResource(R.drawable.baseline_arrow_forward_24), null)
                                3 -> Icon(painterResource(R.drawable.baseline_block_24), null)
                                else -> Icon(
                                    painter = painterResource(R.drawable.baseline_group_24),
                                    contentDescription = stringResource(R.string.friends_tab_friends)
                                )
                            }
                        }
                    )
                }
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
                                        Screen.ChatFriendsScreen.createRoute(friend.uid)
                                    )
                                }
                            },
                            showDelete = true,
                            onDelete = { userViewModel.deleteFriend(friend.uid) },
                            onBlock = { userViewModel.blockUser(friend.uid) },

                            favouriteMessage  = friend.favouriteMessage,
                            achievementCount = friend.achievementCount,
                            theme = theme,

                            // ✅ pass these
                            age = friend.age,
                            gender = friend.gender,
                            city = friend.city
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
                        modifier = Modifier.padding(12.dp),
                        color = theme.textColor
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
                                onBlock = { userViewModel.blockUser(uid) },
                                theme = theme,
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
                        modifier = Modifier.padding(12.dp),
                        color = theme.textColor
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
                                theme = theme,
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
                        modifier = Modifier.padding(12.dp),
                        color = theme.textColor
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
                                theme = theme,
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
        var isSending by remember { mutableStateOf(false) } // ✅ new

        CustomAlertDialog(
            theme = theme,
            onDismiss = { if (!isSending) showAddDialog = false } // ✅ don't close while sending
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title
                Text(
                    text = stringResource(R.string.friends_dialog_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )

                Spacer(Modifier.height(12.dp))

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    enabled = !isSending, // ✅ disable while sending
                    label = { Text(stringResource(R.string.friends_dialog_email_label)) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.textColor,
                        cursorColor = theme.borderColor,
                        focusedBorderColor = theme.borderColor,
                        unfocusedBorderColor = theme.cardContentColor.copy(alpha = 0.5f),
                        focusedLabelColor = theme.textColor,
                        unfocusedLabelColor = theme.textColor.copy(alpha = 0.8f),
                        placeholderColor = theme.cardContentColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAddDialog = false },
                        enabled = !isSending // ✅ prevent dismiss while sending
                    ) {
                        Text(text = stringResource(R.string.friends_dialog_cancel), color = theme.textColor)
                    }

                    Spacer(Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.friends_toast_email_empty),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TextButton
                            }
                            isSending = true // ✅ start loading

                            userViewModel.sendFriendRequestByEmail(
                                email = email,
                                onSuccess = {
                                    isSending = false // ✅ stop loading
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.friends_toast_request_sent),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showAddDialog = false
                                },
                                onFailure = { reason ->
                                    isSending = false // ✅ stop loading
                                    Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isSending && email.isNotBlank()
                    ) {
                        if (isSending) {
                            androidx.compose.material.CircularProgressIndicator(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.friends_sending), color = theme.textColor)
                        } else {
                            Text(text = stringResource(R.string.friends_dialog_send), color = theme.textColor)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun PlusBadgeTiny() {
    Box(
        modifier = Modifier
            .size(14.dp)
            .background(Color(0xFF2E7D32), CircleShape)
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(8.dp)
        )
    }
}
