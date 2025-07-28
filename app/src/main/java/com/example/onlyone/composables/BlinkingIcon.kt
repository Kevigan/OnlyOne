package com.example.onlyone.composables

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun BlinkingIcon(
    painter: Painter,
    contentDescription: String,
    shouldBlink: Boolean,
    baseColor: Color = Color.White,
    blinkColor: Color = Color.Green
) {
    val infiniteTransition = rememberInfiniteTransition()
    val tint by infiniteTransition.animateColor(
        initialValue = baseColor,
        targetValue = if (shouldBlink) blinkColor else baseColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Icon(
        painter = painter,
        contentDescription = contentDescription,
        tint = tint
    )
}

