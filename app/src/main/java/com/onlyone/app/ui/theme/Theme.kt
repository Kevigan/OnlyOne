package com.onlyone.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorPalette = darkColors(
    primary = Color(0xFF00FF00),
    primaryVariant = Color(0xFF433A52),// 0xFF4B007D
    secondary = Color(0xFF76FF03),
    background = Color(0xFF1F002F),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color(0xFFEEEEEE)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF81D4FA),
    primaryVariant = PurpleGrey40,
    secondary = Color(0xFF81D4FA),

    background = Color(0xFFF5F5F5),      // soft light grey
    surface = Color(0xFFFFFFFF),         // still white (for cards, dialogs)
    onBackground = Color(0xFF212121),    // dark grey for readability
    onSurface = Color(0xFF333333),       // softer than pure black

    onPrimary = Color.White,
    onSecondary = Color.White
)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun OnlyOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detect system dark mode
    content: @Composable () -> Unit
) {
    //val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val colors = DarkColorPalette

    androidx.compose.material.MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}