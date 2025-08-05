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
import androidx.compose.ui.unit.dp
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.utils.calculateUpgradeCost
import com.example.onlyone.utils.upgradeCosts
import com.example.onlyone.utils.upgradeSteps

@Composable
fun UpgradeDialog(
    title: String,
    feature: String,
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

    val cost = calculateUpgradeCost(feature, currentValue, 1) // Always 1 step
    val canAfford = userGold >= cost.gold &&
            userRunesRare >= cost.runesRare &&
            userRunesSuperRare >= cost.runesSuperRare &&
            userRunesMegaRare >= cost.runesMegaRare

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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current: $currentValue", color = Color.White)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cost for next step:", style = MaterialTheme.typography.body1, color = Color.White)
                    if (cost.gold > 0) Text("Gold: ${cost.gold}", color = Color.White)
                    if (cost.runesRare > 0) Text("Rare Runes: ${cost.runesRare}", color = Color.White)
                    if (cost.runesSuperRare > 0) Text("Super Rare Runes: ${cost.runesSuperRare}", color = Color.White)
                    if (cost.runesMegaRare > 0) Text("Mega Rare Runes: ${cost.runesMegaRare}", color = Color.White)

                    Spacer(modifier = Modifier.height(8.dp))
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
                                        Toast.makeText(context, "Upgrade successful!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Upgrade failed!", Toast.LENGTH_LONG).show()
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
                                Text("Confirm")
                            }
                        }
                        Button(onClick = onDismiss, enabled = !isLoading) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
