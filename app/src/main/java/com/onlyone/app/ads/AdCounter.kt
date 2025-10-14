package com.onlyone.app.ads

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.adPrefs by preferencesDataStore("ad_prefs")

object AdKeys {
    val SWIPES = intPreferencesKey("swipes_shown")
    val LAST_EPOCH_DAY = longPreferencesKey("last_epoch_day")
}

class AdCounter(private val context: Context) {
    /** Current count for *today* (auto-resets when the saved day != today). */
    val countToday: Flow<Int> = context.adPrefs.data.map { p ->
        val today = LocalDate.now().toEpochDay()
        val last = p[AdKeys.LAST_EPOCH_DAY] ?: today
        if (last == today) (p[AdKeys.SWIPES] ?: 0) else 0
    }

    /** +1 for a newly *shown* random user card (not on tap). */
    suspend fun increment() {
        val today = LocalDate.now().toEpochDay()
        // ✅ let it infer MutablePreferences
        context.adPrefs.edit { p ->
            val today = LocalDate.now().toEpochDay()
            val last = p[AdKeys.LAST_EPOCH_DAY] ?: today
            if (last != today) {
                p[AdKeys.LAST_EPOCH_DAY] = today
                p[AdKeys.SWIPES] = 1
            } else {
                p[AdKeys.SWIPES] = (p[AdKeys.SWIPES] ?: 0) + 1
            }
        }
    }
}
