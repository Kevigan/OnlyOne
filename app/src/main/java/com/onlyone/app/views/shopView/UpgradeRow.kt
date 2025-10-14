package com.onlyone.app.views.shopView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens

@Composable
fun UpgradeRow(
    label: String,
    currentValueText: String,
    onUpgradeClick: () -> Unit,
    theme: ThemeTokens
) {
    val buttonColors =  ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonBackgroundColor,           // ← button fill
        contentColor = theme.textColor,         // ← text & icon tint
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )
    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 21),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp),
        theme = theme
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.body2,
                    color = theme.textColor
                )
                Text(
                    text = currentValueText,
                    style = MaterialTheme.typography.body1,
                    color = theme.textColor
                )
            }
            Button(onClick = onUpgradeClick, colors = buttonColors,) {
                Text(
                    text = stringResource(R.string.shop_upgrade),
                    style = MaterialTheme.typography.button, // or labelLarge for M3
                    color = theme.textColor
                )
            }

        }
    }
}
