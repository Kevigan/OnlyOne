package com.example.onlyone.composables.settings

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

@Composable
fun AboutItemBig() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text("ℹ️ About This App", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Version 1.0 • All rights reserved", color = Color.White.copy(alpha = 0.7f))
        }
    }
}
