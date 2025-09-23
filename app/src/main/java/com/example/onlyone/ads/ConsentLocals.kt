package com.example.onlyone.ads

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.onlyone.privacy.ConsentManager

val LocalConsentManager = staticCompositionLocalOf<ConsentManager> {
    error("LocalConsentManager not provided")
}
