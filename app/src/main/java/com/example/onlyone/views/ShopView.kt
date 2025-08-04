package com.example.onlyone.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.avatarCatalog.AvatarCatalog
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ShopView(userViewModel: UserViewModel) {

    val avatarItems = AvatarCatalog.avatars

    val moodImages = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
    )
    val user by userViewModel.user.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔷 Shop Title
            Text(
                text = "Shop",
                style = MaterialTheme.typography.h5,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🔷 Coins + Value
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gold",
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp, bottom = 16.dp)
                )

                Text(
                    text = user?.gold.toString(),
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // 🟣 Avatars section
        Text(
            text = "Avatars",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
        )
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 21),
            overlayColor = Color.Gray,
            onDismiss = {},
            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            paddingBox2 = PaddingValues(6.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(avatarItems) { avatar ->
                    val alreadyOwned = user?.ownedAvatars?.contains(avatar.id) == true
                    val isSelected = avatar.id == user?.avatarId
                    val canAfford = (user?.gold ?: 0) >= avatar.cost

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = avatar.imageRes),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        when {
                            !alreadyOwned -> {
                                Button(
                                    onClick = {
                                        userViewModel.buyAvatar(
                                            avatarId = avatar.id,
                                            onSuccess = { /* show toast */ },
                                            onFailure = { /* show error */ }
                                        )
                                    },
                                    enabled = canAfford
                                ) {
                                    Text("Buy (${avatar.cost})")
                                }
                            }

                            alreadyOwned && !isSelected -> {
                                Button(
                                    onClick = {
                                        userViewModel.updatePublicProfile(
                                            updates = mapOf("avatarId" to avatar.id),
                                            onSuccess = {},
                                            onFailure = {}
                                        )
                                    }
                                ) {
                                    Text("Select")
                                }
                            }

                            isSelected -> {
                                Button(
                                    onClick = {},
                                    enabled = false
                                ) {
                                    Text("Selected")
                                }
                            }
                        }
                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🟡 Moods section
        Text(
            text = "Moods",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
        )
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 21),
            overlayColor = Color.Gray,
            onDismiss = {},
            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            paddingBox2 = PaddingValues(6.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moodImages) { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Mood",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        if (user != null) {
            val currentLength = user!!.maxMessageLength
            val userGold = user!!.gold

            // 🔵 Display current max message length and upgrade button
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Message Length Limit",
                style = MaterialTheme.typography.h6,
                color = Color.White,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current: $currentLength chars",
                    style = MaterialTheme.typography.body1,
                    color = Color.White
                )

                Button(onClick = { showDialog = true }) {
                    Text("Upgrade")
                }
            }

            // 🔴 Upgrade dialog
            if (showDialog) {
                var upgradeAmount by remember { mutableStateOf("1") }

                val parsedAmount = upgradeAmount.toIntOrNull() ?: 0
                val cost = parsedAmount * 10
                val canAfford = parsedAmount > 0 && userGold >= cost

                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Upgrade Message Length") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = upgradeAmount,
                                onValueChange = { upgradeAmount = it },
                                label = { Text("Levels to upgrade") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your gold: ${user?.gold ?: 0}")
                            Text(
                                "Cost: $cost",
                                color = if (canAfford) Color(0xFF4CAF50) else Color.Red
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val levels = parsedAmount
                                if (levels > 0 && canAfford) {
                                    // call upgrade function multiple times or implement batch upgrade
                                    userViewModel.upgradeMaxMessageLength(
                                        levels = parsedAmount,
                                        onSuccess = { showDialog = false },
                                        onFailure = { /* show error */ }
                                    )
                                    showDialog = false
                                }
                            },
                            enabled = canAfford
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

