package com.example.onlyone.utils

// LocaleUtils.kt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun applyAppLocale(lang: String?) {
    val code = lang?.trim().orEmpty() // you log it's "en"
    val locales = LocaleListCompat.forLanguageTags(code) // "en", "de", "fr", "es", "it"
    AppCompatDelegate.setApplicationLocales(locales)
}

