package com.onlyone.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.onlyone.app.R

@Immutable
object LanguageCatalog {

    // App UI languages
    val APP: List<String> = listOf("en", "de")

    // Chat languages (keep order stable for UX)
    val CHAT: List<String> = listOf(
        "en","de","fr","es","pt","it","nl","pl","ru","tr","uk","cs","ro","hu","sv"
    )

    /** Full display label for a language code */
    @Composable
    fun fullName(code: String): String = when (code) {
        "en" -> stringResource(R.string.lang_english)
        "de" -> stringResource(R.string.lang_german)
        "fr" -> stringResource(R.string.lang_french)
        "es" -> stringResource(R.string.lang_spanish)
        "pt" -> stringResource(R.string.lang_portuguese)
        "it" -> stringResource(R.string.lang_italian)
        "nl" -> stringResource(R.string.lang_dutch)
        "pl" -> stringResource(R.string.lang_polish)
        "ru" -> stringResource(R.string.lang_russian)
        "tr" -> stringResource(R.string.lang_turkish)
        "uk" -> stringResource(R.string.lang_ukrainian)
        "cs" -> stringResource(R.string.lang_czech)
        "ro" -> stringResource(R.string.lang_romanian)
        "hu" -> stringResource(R.string.lang_hungarian)
        "sv" -> stringResource(R.string.lang_swedish)
        "any" -> stringResource(R.string.chat_language_any)
        else -> code.uppercase()
    }

    /** Short label (e.g., EN) if you want compact chips/buttons */
    @Stable
    fun shortLabel(code: String): String = when (code) {
        "any" -> "ANY"
        else  -> code.take(2).uppercase()
    }

    /** Map helper to feed dropdowns (preserves order) */
    @Composable
    fun appOptions(): LinkedHashMap<String, String> =
        linkedMapOf<String, String>().apply {
            APP.forEach { put(it, fullName(it)) }
        }

    @Composable
    fun chatOptions(): LinkedHashMap<String, String> =
        linkedMapOf<String, String>().apply {
            CHAT.forEach { put(it, fullName(it)) }
        }

    /** Chat options including "any" for filters in ChatView */
    @Composable
    fun chatOptionsWithAny(): LinkedHashMap<String, String> =
        linkedMapOf<String, String>().apply {
            put("any", fullName("any"))
            CHAT.forEach { put(it, fullName(it)) }
        }
}
