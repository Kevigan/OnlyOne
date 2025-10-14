// ui/SystemBars.kt
package com.onlyone.app.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.onlyone.app.utils.findActivity

@Composable
fun TransparentSystemBars(darkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val activity = view.context.findActivity() ?: return   // <-- guard against null / wrappers
    SideEffect {
        val window = activity.window

        // Draw your content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Fully transparent bars
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()

        // Android 10+: disable forced contrast scrim on nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = darkIcons
        controller.isAppearanceLightNavigationBars = darkIcons
    }
}

