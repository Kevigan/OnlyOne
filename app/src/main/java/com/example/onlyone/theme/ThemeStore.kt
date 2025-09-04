package com.example.onlyone.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ✅ TOP-LEVEL delegate (not inside a class) + unique name
private val Context.themeDataStore by preferencesDataStore(name = "user_theme_prefs")
private object Keys { val THEME_ID = stringPreferencesKey("selected_theme_id") }

@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val app: Context
) {
    val themeIdFlow: Flow<ThemeId> =
        app.themeDataStore.data.map { p ->
            p[Keys.THEME_ID]?.let { runCatching { ThemeId.valueOf(it) }.getOrNull() } ?: ThemeId.LIGHT
        }

    suspend fun setTheme(id: ThemeId) {
        app.themeDataStore.edit { it[Keys.THEME_ID] = id.name }
    }
}
