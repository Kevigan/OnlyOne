package com.onlyone.app.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// ✅ TOP-LEVEL delegate (not inside a class) + unique name
private val Context.themeDataStore by preferencesDataStore(name = "user_theme_prefs")
private object Keys { val THEME_ID = stringPreferencesKey("selected_theme_id") }

@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val app: Context
) {
    /** Continuous stream of the selected theme; defaults to LIGHT if not set or unparsable. */
    val themeIdFlow: Flow<ThemeId> =
        app.themeDataStore.data.map { p ->
            p[Keys.THEME_ID]
                ?.let { runCatching { ThemeId.valueOf(it) }.getOrNull() }
                ?: ThemeId.LIGHT
        }

    /** Persist a new theme selection. */
    suspend fun setTheme(id: ThemeId) {
        app.themeDataStore.edit { it[Keys.THEME_ID] = id.name }
    }

    /** One-shot suspend read of the current theme (useful in non-VM contexts). */
    suspend fun currentThemeId(): ThemeId = themeIdFlow.first()

    /**
     * Synchronous (blocking) read for initial VM state to avoid theme flicker.
     * Safe to call during ViewModel construction; uses Dispatchers.IO under the hood.
     */
    fun blockingInitialThemeId(): ThemeId = runBlocking(Dispatchers.IO) {
        currentThemeId()
    }
}
