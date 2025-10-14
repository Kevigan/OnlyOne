package com.onlyone.app.views.settingsView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens

@Composable
fun SettingsItemRow(title: String, onClick: () -> Unit, theme: ThemeTokens) {
    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(percent = 50),
        overlayColor = Color.DarkGray.copy(alpha = 0.4f),
        borderColor = Color.LightGray,
        borderWidth = 1.dp,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        paddingBox2 = PaddingValues(6.dp),
        theme = theme
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.body1,
            color = theme.textColor
        )
    }
}