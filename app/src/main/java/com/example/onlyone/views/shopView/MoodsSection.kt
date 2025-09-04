// MoodsSection.kt
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
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.MoodCatalog
import com.example.onlyone.composables.MoodCategory
import com.example.onlyone.data.UserComposite
import com.example.onlyone.theme.ThemeTokens

@Composable
fun MoodsSection(
    user: UserComposite?,
    loadingMoodId: Int?,
    onBuyMood: (Int) -> Unit,
    onSelectMood: (Int) -> Unit,
    theme: ThemeTokens
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(MoodCategory.ALL) }
    val buttonColors =  ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonColor,           // ← button fill
        contentColor = theme.textColor,         // ← text & icon tint
        disabledBackgroundColor = theme.cardBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )

    val moodItems = remember(selectedCategory) {
        when (selectedCategory) {
            MoodCategory.ALL        -> MoodCatalog.moods
            MoodCategory.ACTIVITIES -> MoodCatalog.moods.filter { it.category == MoodCategory.ACTIVITIES }
            MoodCategory.LOVE       -> MoodCatalog.moods.filter { it.category == MoodCategory.LOVE }
        }
    }

    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 12),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp),
        theme = theme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp)
        ) {
            // Header: Title + Dropdown with arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.common_moods),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp, bottom = 4.dp)
                )

                Box {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "")
                    TextButton(onClick = { expanded = true }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(stringResource(selectedCategory.labelRes), color = theme.textColor)
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = stringResource(R.string.common_open_menu),
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
                        MoodCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                }
                            ) {
                                Text(text = stringResource(cat.labelRes), color = theme.textColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Items (filtered)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moodItems) { mood ->
                    val alreadyOwned = user?.ownedMoods?.contains(mood.id) == true
                    val isSelected = mood.id == user?.moodId
                    val canAfford = (user?.gold ?: 0) >= mood.cost
                    val isLoading = loadingMoodId == mood.id

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = mood.imageRes),
                            contentDescription = stringResource(R.string.common_mood),
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
                                    onClick = { onBuyMood(mood.id) },
                                    colors = buttonColors,
                                    enabled = canAfford && !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = theme.textColor
                                        )
                                    } else {
                                        Text(stringResource(R.string.common_buy_with_cost, mood.cost), color = theme.textColor)
                                    }
                                }
                            }
                            alreadyOwned && !isSelected -> {
                                Button(onClick = { onSelectMood(mood.id) }, colors = buttonColors, enabled = !isLoading) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = theme.textColor
                                        )
                                    } else {
                                        Text(stringResource(R.string.common_select), color = theme.textColor)
                                    }
                                }
                            }
                            else -> {
                                Button(onClick = {}, colors = buttonColors, enabled = false) {
                                    Text(stringResource(R.string.common_selected), color = theme.textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
