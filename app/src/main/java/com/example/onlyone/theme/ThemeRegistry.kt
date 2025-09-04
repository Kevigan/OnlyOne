// ThemeRegistry.kt
package com.example.onlyone.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.onlyone.R

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
    val gradientColor1: Color = overlayColor,  // defaults if not set
    val gradientColor2: Color = overlayColor,
    val buttonColor: Color = Color.White
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
            cardContentColor = Color(0xFF101215)
        )
        ThemeId.DARK -> ThemeTokens(
            backgroundRes = R.drawable.background_new_2,
            overlayColor = Color(0x882ECC71),        // translucent lighter green overlay
            borderColor = Color(0xFF7DFFB3),         // fresh mint/lime green border
            textColor = Color.Yellow,           // dark forest green text
            dialogBackground = Color(0xFF2ECC71),    // lighter green dialog background
            dialogContentColor = Color(0xFF0B3D2E),  // dark forest green for readability
            cardBackground = Color(0xFF28A745),      // slightly darker green card background
            cardContentColor = Color(0xFF0B3D2E),    // consistent dark green text on cards
            gradientColor1 = Color(0xFF0D3B2E),  // top gradient
            gradientColor2 = Color(0xFF001F3F),
            buttonColor = Color.Gray
        )
        ThemeId.OCEAN -> ThemeTokens(
            backgroundRes = R.drawable.background_new_6, // your existing bg
            overlayColor = Color(0x880A232A),
            borderColor = Color(0xFF26C6DA),
            textColor = Color(0xFFE1F5FE),
            dialogBackground = Color(0xFF0E272E),
            dialogContentColor = Color(0xFFE1F5FE),
            cardBackground = Color(0xFF0E272E),
            cardContentColor = Color(0xFFE1F5FE)
        )
    }
}
