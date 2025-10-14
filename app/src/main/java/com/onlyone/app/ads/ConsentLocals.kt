package com.onlyone.app.ads

import androidx.compose.runtime.staticCompositionLocalOf
import com.onlyone.app.privacy.ConsentManager

val LocalConsentManager = staticCompositionLocalOf<ConsentManager> {
    error("LocalConsentManager not provided")
}
