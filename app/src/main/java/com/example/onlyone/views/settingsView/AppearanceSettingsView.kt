package com.example.onlyone.views.settingsView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.theme.ThemeTokens

@Composable
fun AppearanceSettingsView(theme: ThemeTokens,) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text("🎨 Appearance", color = theme.textColor, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Customize your app appearance here.", color = theme.textColor.copy(alpha = 0.7f))
        }
    }
}
