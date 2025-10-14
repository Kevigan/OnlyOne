import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.composables.MoodCategory
import com.onlyone.app.composables.ownedMoodItems
import com.onlyone.app.theme.ThemeTokens

@Composable
fun MoodPickerDialog(
    theme: ThemeTokens,
    ownedMoodIds: List<Int>,
    currentMoodId: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Build owned items once
    val ownedItems = remember(ownedMoodIds) { ownedMoodItems(ownedMoodIds) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(MoodCategory.ALL) }

    // Only show categories that actually exist among the owned items (plus ALL)
    val availableCategories = remember(ownedItems) {
        val present = ownedItems.map { it.category }.toSet()
        listOf(MoodCategory.ALL) + MoodCategory.values().filter { it != MoodCategory.ALL && it in present }
    }

    // Apply category filter to owned items
    val filteredItems = remember(selectedCategory, ownedItems) {
        when (selectedCategory) {
            MoodCategory.ALL        -> ownedItems
            MoodCategory.ACTIVITIES -> ownedItems.filter { it.category == MoodCategory.ACTIVITIES }
            MoodCategory.LOVE       -> ownedItems.filter { it.category == MoodCategory.LOVE }
        }
    }

    // 🔒 Fixed sizing so dialog doesn't jump
    val dialogWidth = 360.dp
    val gridHeight = 320.dp

    CustomAlertDialog(
        theme = theme,
        borderColor = theme.borderColor,
        onDismiss = onDismiss
    ) {
        // Make the dialog the same width you had before
        Column(
            modifier = Modifier
                .width(dialogWidth)
                .fillMaxWidth()
        ) {
            // Title
            Text(
                text = "Choose mood", // keep hardcoded as you noted
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )

            Spacer(Modifier.height(12.dp))

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
                            Text(
                                text = stringResource(selectedCategory.labelRes),
                                color = theme.textColor
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null,
                                tint = theme.textColor,
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
                                Text(
                                    text = stringResource(cat.labelRes),
                                    color = theme.textColor
                                )
                            }
                        }
                    }
                }
            }

            // Fixed-height viewport for the grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
            ) {
                if (filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No owned moods yet.", color = theme.textColor)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(56.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems) { mood ->
                            val selected = (mood.id == currentMoodId)
                            Image(
                                painter = painterResource(id = mood.imageRes),
                                contentDescription = "Mood ${mood.id}",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) theme.borderColor
                                        else theme.cardContentColor.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onSelect(mood.id)
                                        onDismiss()
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = theme.textColor)
                }
            }
        }
    }

}
