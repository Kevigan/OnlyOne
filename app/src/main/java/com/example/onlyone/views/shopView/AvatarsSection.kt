package com.example.onlyone.views.shopView

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.AvatarCatalog
import com.example.onlyone.composables.AvatarCategory
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.data.UserComposite

@Composable
fun AvatarsSection(
    user: UserComposite?,
    loadingAvatarId: Int?,
    onBuyAvatar: (Int) -> Unit,
    onSelectAvatar: (Int) -> Unit
) {
    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(AvatarCategory.ALL) }

// Filter the avatars by selected category
    val filteredAvatars = remember(selectedCategory) {
        when (selectedCategory) {
            AvatarCategory.ALL    -> AvatarCatalog.avatars
            AvatarCategory.GHOSTS -> AvatarCatalog.avatars.filter { it.category == AvatarCategory.GHOSTS }
            AvatarCategory.ALIENS -> AvatarCatalog.avatars.filter { it.category == AvatarCategory.ALIENS }
            AvatarCategory.Humans -> AvatarCatalog.avatars.filter { it.category == AvatarCategory.Humans }
        }
    }

    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 12),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp)
        ) {
            // Header row: Title + Category dropdown
            // Header row: Title + Category dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.common_avatars),
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp, bottom = 4.dp)
                )

                // Text button with a down arrow that rotates on open
                Box {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "")
                    TextButton(onClick = { expanded = true }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(stringResource(selectedCategory.labelRes), color = Color.White)
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = stringResource(R.string.common_open_menu),
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AvatarCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                }
                            ) {
                                Text(text = stringResource(cat.labelRes))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Items list (filtered)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredAvatars) { avatar ->
                    val alreadyOwned = user?.ownedAvatars?.contains(avatar.id) == true
                    val isSelected = avatar.id == user?.avatarId
                    val canAfford = (user?.gold ?: 0) >= avatar.cost
                    val isLoading = loadingAvatarId == avatar.id

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = avatar.imageRes),
                            contentDescription = stringResource(R.string.common_avatar),
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
                                    onClick = { onBuyAvatar(avatar.id) },
                                    enabled = canAfford && !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(stringResource(R.string.common_buy_with_cost, avatar.cost))
                                    }
                                }
                            }
                            alreadyOwned && !isSelected -> {
                                Button(
                                    onClick = { onSelectAvatar(avatar.id) },
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(stringResource(R.string.common_select))
                                    }
                                }
                            }
                            else -> {
                                Button(onClick = {}, enabled = false) {
                                    Text(stringResource(R.string.common_selected))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


enum class AvatarAction { BUY, SELECT }