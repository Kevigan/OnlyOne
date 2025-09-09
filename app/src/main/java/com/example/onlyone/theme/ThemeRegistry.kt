// ThemeRegistry.kt
package com.example.onlyone.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.onlyone.R

// theme/ThemeTokens.kt
enum class ThemeId { LIGHT, DARK, OCEAN }

data class ThemeTokens(
    @DrawableRes val backgroundRes: Int?,
    val overlayColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val dialogBackground: Color,
    val dialogContentColor: Color,
    val cardBackground: Color,
    val cardContentColor: Color,
    val gradientColor1: Color = overlayColor,
    val gradientColor2: Color = overlayColor,
    val buttonColor: Color = Color.White,
    // NEW: whether status/nav bar icons should be dark (true) or light (false)
    val systemBarDarkIcons: Boolean = true
)

object ThemeRegistry {
    fun tokens(id: ThemeId) = when (id) {
        ThemeId.LIGHT -> ThemeTokens(
            backgroundRes = R.drawable.background_new_4,
            overlayColor = Color(0x99FFFFFF),
            borderColor = Color(0xFF3F51B5),
            textColor = Color(0xFF101215),
            dialogBackground = Color.White,
            dialogContentColor = Color(0xFF101215),
            cardBackground = Color.White,
            cardContentColor = Color(0xFF101215),
            systemBarDarkIcons = true       // dark icons on light bg
        )
        ThemeId.DARK -> ThemeTokens(
            backgroundRes = R.drawable.background_new_2,
            overlayColor = Color(0x882ECC71),
            borderColor = Color(0xFF7DFFB3),
            textColor = Color.Yellow,
            dialogBackground = Color(0xFF2ECC71),
            dialogContentColor = Color(0xFF0B3D2E),
            cardBackground = Color(0xFF28A745),
            cardContentColor = Color(0xFF0B3D2E),
            gradientColor1 = Color(0xFF0D3B2E),
            gradientColor2 = Color(0xFF001F3F),
            buttonColor = Color.Gray,
            systemBarDarkIcons = false      // light icons on dark bg
        )
        ThemeId.OCEAN -> ThemeTokens(
            backgroundRes = R.drawable.background_new_6,
            overlayColor = Color(0x880A232A),
            borderColor = Color(0xFF26C6DA),
            textColor = Color(0xFFE1F5FE),
            dialogBackground = Color(0xFF0E272E),
            dialogContentColor = Color(0xFFE1F5FE),
            cardBackground = Color(0xFF0E272E),
            cardContentColor = Color(0xFFE1F5FE),
            systemBarDarkIcons = false      // light icons on dark bg
        )
    }
}
