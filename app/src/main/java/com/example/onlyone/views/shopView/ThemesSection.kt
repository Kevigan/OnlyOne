// ThemesSection.kt
package com.example.onlyone.views.shopView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.theme.ThemeId
import com.example.onlyone.theme.ThemeRegistry
import com.example.onlyone.theme.ThemeTokens

@Composable
fun ThemesSection(
    currentThemeId: ThemeId,
    ownedThemeIds: List<Int>,              // 1-based ids from DB (e.g., [1,2])
    loadingThemeId: ThemeId?,
    onSelectTheme: (ThemeId) -> Unit,
    onBuyTheme: (ThemeId) -> Unit,
    ids: List<ThemeId> = ThemeId.values().toList(),
    theme: ThemeTokens
) {
    // Cache membership for O(1) lookups
    val ownedSet = remember(ownedThemeIds) { ownedThemeIds.toSet() }

    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonBackgroundColor,
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )

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
            Text(
                text = stringResource(R.string.shop_themes_title),
                style = MaterialTheme.typography.h6,
                color = theme.textColor,
                modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // key by stable 1-based id to avoid recomposition artifacts
                items(ids, key = { it.id }) { id ->
                    val tokens: ThemeTokens = ThemeRegistry.tokens(id)
                    val isSelected = id == currentThemeId
                    val isLoading = loadingThemeId == id
                    val isOwned = ownedSet.contains(id.id)   // ← FIX: use 1-based id

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(150.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(90.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            tokens.backgroundRes?.let { resId ->
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = stringResource(R.string.shop_theme_preview),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(modifier = Modifier.matchParentSize())
                            } ?: run {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(MaterialTheme.colors.background)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(tokens.borderColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            isSelected -> {
                                Button(onClick = {}, colors = buttonColors, enabled = false) {
                                    Text(stringResource(R.string.common_selected), color = theme.textColor)
                                }
                            }
                            isOwned -> {
                                Button(onClick = { onSelectTheme(id) }, colors = buttonColors, enabled = !isLoading) {
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
                                Button(onClick = { onBuyTheme(id) }, colors = buttonColors, enabled = !isLoading) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = theme.textColor
                                        )
                                    } else {
                                        Text(stringResource(R.string.common_buy), color = theme.textColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
