package com.example.onlyone.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.ReceivedMessageItem
import com.example.onlyone.composables.UserStatsCardContent
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainView(
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel
) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colors.background

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
                Text("Hello User", style = MaterialTheme.typography.h6, color = Color.White)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            // 🟣 2/8 — Overlay
            CustomColorOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                onDismiss = {}
            ) {
                UserStatsCardContent(
                    messagesLeft = "12/25",
                    dailyPoints = "240",
                    pointsBank = "1820",
                    rank = "S-Rank",
                    avatarResId = R.drawable.baseline_tag_faces_24
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🟣 5/8 — Message list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f)
            ) {
                Text("Received Messages", style = MaterialTheme.typography.h6, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                val messages = listOf(
                    Triple("Alice", "Hey, did you check out the new update?", "24hrs"),
                    Triple("Bob", "Got your message, will reply soon!", "12hrs"),
                    Triple("Charlie", "Let's meet up tomorrow around noon", "6hrs"),
                    Triple("Diana", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "48hrs"),
                    Triple("Diana", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "48hrs"),
                    Triple("Diana", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "48hrs"),
                    Triple("Diana", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "48hrs")
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { (name, message, expiration) ->
                        ReceivedMessageItem(
                            avatarResId = R.drawable.baseline_tag_faces_24,
                            name = name,
                            message = message,
                            expiration = expiration,
                            onClick = { } //TODO
                        )
                    }
                }
            }
        }
    }
}