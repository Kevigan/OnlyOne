package com.onlyone.app.views.shopView

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.deriveLevelFromAbsolute
import com.onlyone.app.utils.maxLevels
import com.onlyone.app.utils.nextUpgradeCostOrNull

@Composable
fun UpgradeDialog(
    title: String,
    feature: String, // "maxMessageLength" | "maxSwipes" | "maxMoodLength" | etc.
    currentValue: Int, // absolute value from users_public
    userGold: Int,
    userRunesRare: Int,
    userRunesSuperRare: Int,
    userRunesMegaRare: Int,
    onConfirm: (onResult: (success: Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    theme: ThemeTokens
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Treat param as absolute and derive level
    val currentAbsolute = currentValue
    val level = deriveLevelFromAbsolute(feature, currentAbsolute) // 0-based
    val totalLevels = maxLevels(feature)
    val humanLevel = (level + 1).coerceAtMost(totalLevels).coerceAtLeast(1)
    val isMaxed = level >= totalLevels

    // Cost for the NEXT upgrade level (null if maxed)
    val nextCost = nextUpgradeCostOrNull(feature, currentAbsolute)

    val canAfford = !isMaxed && nextCost != null &&
            userGold >= nextCost.gold &&
            userRunesRare >= nextCost.runesRare &&
            userRunesSuperRare >= nextCost.runesSuperRare &&
            userRunesMegaRare >= nextCost.runesMegaRare

    // Localized "Current: …"
    val currentValueText = when (feature) {
        "maxMessageLength" -> pluralStringResource(
            R.plurals.common_chars, currentAbsolute, currentAbsolute
        )
        "maxSwipes" -> pluralStringResource(
            R.plurals.common_swipes_per_day, currentAbsolute, currentAbsolute
        )
        "maxMoodLength" -> pluralStringResource(
            R.plurals.common_chars, currentAbsolute, currentAbsolute
        )
        else -> currentAbsolute.toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            CustomColorOverlay(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 21),
                overlayColor = Color.Gray,
                onDismiss = onDismiss,
                paddingBox1 = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                paddingBox2 = PaddingValues(12.dp),
                theme = theme,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, style = MaterialTheme.typography.h6, color = theme.textColor)

                    // Level progress (e.g., "Level 3 / 15" or "Maxed")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isMaxed) {
                            stringResource(R.string.shop_upgrade_level_maxed)
                        } else {
                            stringResource(
                                R.string.shop_upgrade_level_progress,
                                humanLevel,
                                totalLevels
                            )
                        },
                        color = theme.textColor
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.common_current, currentValueText),
                        color = theme.textColor
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.shop_cost_header),
                        style = MaterialTheme.typography.body1,
                        color = theme.textColor
                    )

                    // Show next step cost (or "no further upgrades")
                    if (isMaxed || nextCost == null) {
                        Text(
                            text = stringResource(R.string.shop_no_further_upgrades),
                            color = theme.textColor
                        )
                    } else {
                        if (nextCost.gold > 0) {
                            Text(
                                text = stringResource(R.string.common_gold_with_amount, nextCost.gold),
                                color = theme.textColor
                            )
                        }
                        if (nextCost.runesRare > 0) {
                            Text(
                                text = stringResource(R.string.common_runes_rare, nextCost.runesRare),
                                color = theme.textColor
                            )
                        }
                        if (nextCost.runesSuperRare > 0) {
                            Text(
                                text = stringResource(R.string.common_runes_super_rare, nextCost.runesSuperRare),
                                color = theme.textColor
                            )
                        }
                        if (nextCost.runesMegaRare > 0) {
                            Text(
                                text = stringResource(R.string.common_runes_mega_rare, nextCost.runesMegaRare),
                                color = theme.textColor
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                onConfirm { success ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.shop_upgrade_success),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.shop_upgrade_failed),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            enabled = canAfford && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isMaxed) {
                                        stringResource(R.string.shop_upgrade_button_maxed)
                                    } else {
                                        stringResource(R.string.common_confirm)
                                    },
                                    color = theme.textColor
                                )
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(R.string.common_cancel), color = theme.textColor)
                        }
                    }
                }
            }
        }
    }
}
