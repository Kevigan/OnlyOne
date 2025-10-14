// com.onlyone.app.utils.LocaleUtils.kt
package com.onlyone.app.utils

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.*
import java.util.Locale

private val localeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
private var applyJob: Job? = null
private var lastRequestedCode: String? = null

/** Reduce a language tag to the primary language ("en-US" -> "en"). */
fun normalizeLang(tag: String?): String {
    if (tag.isNullOrBlank()) return ""
    // You can extend aliases here if you support "pt-BR" as separate, etc.
    val primary = tag.trim().lowercase(Locale.ROOT).split('-', '_').firstOrNull().orEmpty()
    return when (primary) {
        "zh" -> "zh" // keep as needed
        else -> primary
    }
}

/** Read the currently applied app locale (falls back to device default). */
fun currentAppLanguageCode(): String {
    val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val fromApp = normalizeLang(tags)
    if (fromApp.isNotEmpty()) return fromApp
    return normalizeLang(Locale.getDefault().toLanguageTag()).ifEmpty { "en" }
}

/**
 * Debounced, main-thread-safe setter. Will no-op if the effective
 * app locale is already the target primary language.
 */
fun applyAppLocale(lang: String?) {
    val code = normalizeLang(lang)
    if (code.isEmpty()) return

    // If only region changed but primary stayed, we intentionally skip (UI is language-driven).
    val currentPrimary = currentAppLanguageCode()
    if (code == currentPrimary) return

    lastRequestedCode = code
    applyJob?.cancel()
    applyJob = localeScope.launch {
        // Coalesce rapid taps
        delay(350)

        val target = lastRequestedCode ?: return@launch
        val cur = currentAppLanguageCode()
        if (target == cur) return@launch

        // Give Compose a frame to close menus etc. before recreate
        withContext(Dispatchers.Main) { delay(16) }

        try {
            // Use only the primary subtag when setting, to match our normalizeLang()
            val locales = LocaleListCompat.forLanguageTags(target)
            AppCompatDelegate.setApplicationLocales(locales)
        } catch (t: Throwable) {
            Log.e("applyAppLocale", "Failed to apply locale=$target", t)
        }
    }
}
