// utils/AvatarOwnedFilter.kt (or next to your AvatarCatalog)
package com.example.onlyone.ui.avatar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.composables.AvatarCatalog
import com.example.onlyone.composables.AvatarCategory
import com.example.onlyone.composables.AvatarItem
import com.example.onlyone.R

fun ownedAvatarItems(ownedIds: Collection<Int>): List<AvatarItem> {
    if (ownedIds.isEmpty()) return emptyList()
    val set = ownedIds.toSet()
    return AvatarCatalog.avatars.filter { it.id in set }
}

@Composable
fun AvatarPickerDialog(
    ownedAvatarIds: List<Int>,
    currentAvatarId: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Build owned items once
    val ownedItems = remember(ownedAvatarIds) { ownedAvatarItems(ownedAvatarIds) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(AvatarCategory.ALL) }

    // Only show categories that actually exist among the owned items (plus ALL)
    val availableCategories = remember(ownedItems) {
        val present = ownedItems.map { it.category }.toSet()
        listOf(AvatarCategory.ALL) + AvatarCategory.values().filter { it != AvatarCategory.ALL && it in present }
    }

    // Apply category filter to owned items
    val filteredItems = remember(selectedCategory, ownedItems) {
        when (selectedCategory) {
            AvatarCategory.ALL -> ownedItems
            else               -> ownedItems.filter { it.category == selectedCategory }
        }
    }

    // 🔒 Fixed sizing
    val dialogWidth = 360.dp           // tweak if needed
    val gridHeight = 320.dp            // viewport for the grid area

    AlertDialog(
        modifier = Modifier.width(dialogWidth),   // fixed dialog width
        onDismissRequest = onDismiss,
        title = { Text("Choose avatar") },        // simple title; use a string res if you prefer
        text = {
            Column(Modifier.fillMaxWidth()) {
                // Category selector with rotating arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "")
                        TextButton(onClick = { expanded = true }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(stringResource(selectedCategory.labelRes))
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
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
                            availableCategories.forEach { cat ->
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

                // 🔒 Fixed-height viewport so the dialog height stays constant
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                ) {
                    if (filteredItems.isEmpty()) {
                        // Centered placeholder but within the same fixed-height box
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No owned avatars yet.")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(56.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredItems) { avatar ->
                                val selected = (avatar.id == currentAvatarId)
                                Image(
                                    painter = painterResource(id = avatar.imageRes),
                                    contentDescription = "Avatar ${avatar.id}",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) Color.Cyan else Color.White.copy(alpha = 0.25f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onSelect(avatar.id)
                                            onDismiss()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}



