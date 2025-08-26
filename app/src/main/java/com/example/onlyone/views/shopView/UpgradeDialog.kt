package com.example.onlyone.views.shopView

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
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.utils.calculateUpgradeCost
import com.example.onlyone.utils.upgradeCosts
import com.example.onlyone.utils.upgradeSteps

@Composable
fun UpgradeDialog(
    title: String,
    feature: String, // "maxMessageLength" | "maxSwipes" (others show raw number)
    currentValue: Int,
    userGold: Int,
    userRunesRare: Int,
    userRunesSuperRare: Int,
    userRunesMegaRare: Int,
    onConfirm: (onResult: (success: Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val cost = calculateUpgradeCost(feature, currentValue, 1) // always 1 step
    val canAfford = userGold >= cost.gold &&
            userRunesRare >= cost.runesRare &&
            userRunesSuperRare >= cost.runesSuperRare &&
            userRunesMegaRare >= cost.runesMegaRare

    // Localized "Current: …"
    val currentValueText = when (feature) {
        "maxMessageLength" -> pluralStringResource(
            R.plurals.common_chars, currentValue, currentValue
        )
        "maxSwipes" -> pluralStringResource(
            R.plurals.common_swipes_per_day, currentValue, currentValue
        )
        "maxMoodLength" -> pluralStringResource(
            R.plurals.common_chars, currentValue, currentValue
        )
        else -> currentValue.toString()
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
                paddingBox2 = PaddingValues(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, style = MaterialTheme.typography.h6, color = Color.White)

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.common_current, currentValueText),
                        color = Color.White
                    )

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.shop_cost_header),
                        style = MaterialTheme.typography.body1,
                        color = Color.White
                    )

                    if (cost.gold > 0) {
                        Text(
                            text = stringResource(R.string.common_gold_with_amount, cost.gold),
                            color = Color.White
                        )
                    }
                    if (cost.runesRare > 0) {
                        Text(
                            text = stringResource(R.string.common_runes_rare, cost.runesRare),
                            color = Color.White
                        )
                    }
                    if (cost.runesSuperRare > 0) {
                        Text(
                            text = stringResource(R.string.common_runes_super_rare, cost.runesSuperRare),
                            color = Color.White
                        )
                    }
                    if (cost.runesMegaRare > 0) {
                        Text(
                            text = stringResource(R.string.common_runes_mega_rare, cost.runesMegaRare),
                            color = Color.White
                        )
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
                                Text(stringResource(R.string.common_confirm))
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
                            Text(stringResource(R.string.common_cancel))
                        }

                    }
                }
            }
        }
    }
}
