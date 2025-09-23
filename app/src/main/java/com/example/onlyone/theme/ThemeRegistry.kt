// ThemeRegistry.kt
package com.example.onlyone.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.onlyone.R

// ────────────────────────────────────────────────────────────────────────────────
// Theme model
// ────────────────────────────────────────────────────────────────────────────────
enum class ThemeId(val id: Int) {
    LIGHT(1),
    DARK(2),
    OCEAN(3);

    companion object {
        /** Safe lookup from numeric (1-based) id; null if not found */
        fun fromId(id: Int): ThemeId? = values().firstOrNull { it.id == id }
    }
}

data class ThemeTokens(
    @DrawableRes val backgroundRes: Int?,
    val borderColor: Color,
    val textColor: Color,
    val buttonBackgroundColor: Color = Color.White,
    val disabledButtonBackground: Color,
    val cardContentColor: Color,
    val gradientColor1: Color,
    val gradientColor2: Color,
    // Whether status/nav bar icons should be dark (true) or light (false)
    val systemBarDarkIcons: Boolean = true
)

// ────────────────────────────────────────────────────────────────────────────────
// Theme registry (fetch by enum or by numeric id)
// ────────────────────────────────────────────────────────────────────────────────
object ThemeRegistry {

    /** Primary accessor: by enum */
    fun tokens(id: ThemeId) = when (id) {
        ThemeId.LIGHT -> ThemeTokens(
            backgroundRes = R.drawable.cloud_background_day,
            borderColor = Color(0xFF3F51B5),
            textColor = Color(0xFF101215),
            buttonBackgroundColor = Color(0xFFE3F2FD),
            disabledButtonBackground = Color(0xFFB0BEC5),
            cardContentColor = Color.White,
            gradientColor1 = Color(0xFF64B5F6),
            gradientColor2 = Color(0xFF1976D2),
            systemBarDarkIcons = true
        )
        ThemeId.DARK -> ThemeTokens(
            backgroundRes = R.drawable.cloud_background_night,
            borderColor = Color.White,
            textColor = Color(0xFFE0E0E0),
            buttonBackgroundColor = Color(0xFF252545),
            disabledButtonBackground = Color(0xFF145A5A),
            cardContentColor = Color(0xFF08081A),
            gradientColor1 = Color(0xFF1976D2),
            gradientColor2 = Color(0xFF0A2A6B),
            systemBarDarkIcons = false
        )
        ThemeId.OCEAN -> ThemeTokens(
            backgroundRes = R.drawable.background_new_6,
            borderColor = Color(0xFF26C6DA),
            textColor = Color(0xFFE1F5FE),
            buttonBackgroundColor = Color(0xFF013E5A), // explicit to avoid relying on default
            disabledButtonBackground = Color(0xFF0E272E),
            cardContentColor = Color(0xFFE1F5FE),
            gradientColor1 = Color(0xFF0D3B2E),
            gradientColor2 = Color(0xFF001F3F),
            systemBarDarkIcons = false
        )
    }

    /** Convenience: fetch by 1-based numeric id; LIGHT as safe fallback */
    fun tokensById(numericId: Int): ThemeTokens =
        tokens(ThemeId.fromId(numericId) ?: ThemeId.LIGHT)

    /** Optional helpers */
    val defaultId: Int get() = ThemeId.LIGHT.id
    fun idOf(theme: ThemeId): Int = theme.id
}
