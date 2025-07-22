package com.example.onlyone.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@Composable
fun TopSnackbar(message: String) {
    val yOffset by animateDpAsState(
        targetValue = if (message.isNotBlank()) 48.dp else (-100).dp,
        label = "snackbar-slide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            color = Color(0xFF333333),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            modifier = Modifier
                .offset(y = yOffset)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (message.isBlank()) "New message" else message,
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}




