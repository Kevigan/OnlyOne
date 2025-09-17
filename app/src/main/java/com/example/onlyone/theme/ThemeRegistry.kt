// ThemeRegistry.kt
package com.example.onlyone.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.onlyone.R

// theme/ThemeTokens.kt
enum class ThemeId { LIGHT, DARK, OCEAN }

data class ThemeTokens(
    @DrawableRes val backgroundRes: Int?,
    val borderColor: Color,
    val textColor: Color,
    val buttonBackgroundColor: Color = Color.White,
    val disabledButtonBackground: Color,
    val cardContentColor: Color,
    val gradientColor1: Color,
    val gradientColor2: Color,
    // NEW: whether status/nav bar icons should be dark (true) or light (false)
    val systemBarDarkIcons: Boolean = true
)

object ThemeRegistry {
    fun tokens(id: ThemeId) = when (id) {
        ThemeId.LIGHT -> ThemeTokens(
            backgroundRes = R.drawable.cloud_background_day,
            borderColor = Color(0xFF3F51B5),         // medium indigo, fits sky theme
            textColor = Color(0xFF101215),           // dark text for readability
            buttonBackgroundColor = Color(0xFFE3F2FD), // very light blue background for buttons
            disabledButtonBackground = Color(0xFFB0BEC5), // soft gray-blue for disabled
            cardContentColor = Color.White,          // light cards on bright background
            gradientColor1 = Color(0xFF64B5F6),      // bright sky blue
            gradientColor2 = Color(0xFF1976D2),      // deeper but still bright blue
            systemBarDarkIcons = true                // dark icons for light bg
        )

        ThemeId.DARK -> ThemeTokens(
            backgroundRes = R.drawable.cloud_background_night,
            borderColor = Color.White,              // crisp white border
            textColor = Color(0xFFE0E0E0),          // soft muted white
            buttonBackgroundColor = Color(0xFF252545), // slightly darker bluish-gray
            disabledButtonBackground = Color(0xFF145A5A), // darker teal-cyan
            cardContentColor = Color(0xFF08081A),   // very deep navy
            gradientColor1 = Color(0xFF1976D2),     // darker blue (was #2196F3)
            gradientColor2 = Color(0xFF0A2A6B),     // darker deep blue (was #0D47A1)
            systemBarDarkIcons = false
        )
        ThemeId.OCEAN -> ThemeTokens(
            backgroundRes = R.drawable.background_new_6,
            borderColor = Color(0xFF26C6DA),
            textColor = Color(0xFFE1F5FE),
            disabledButtonBackground = Color(0xFF0E272E),
            cardContentColor = Color(0xFFE1F5FE),
            gradientColor1 = Color(0xFF0D3B2E),
            gradientColor2 = Color(0xFF001F3F),
            systemBarDarkIcons = false      // light icons on dark bg
        )
    }
}
