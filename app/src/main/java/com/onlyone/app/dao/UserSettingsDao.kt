package com.onlyone.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onlyone.app.data.LocalUserSettings

@Dao
interface UserSettingsDao {

    // Get the whole row
    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): LocalUserSettings?

    // Lightweight read for startup
    @Query("SELECT language FROM user_settings WHERE id = 1")
    suspend fun getLanguage(): String?

    // Upsert the row
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: LocalUserSettings)

    // Convenience setters (return number of rows updated)
    @Query("UPDATE user_settings SET language = :lang WHERE id = 1")
    suspend fun setLanguage(lang: String): Int

    @Query("UPDATE user_settings SET notifyMessages = :enabled WHERE id = 1")
    suspend fun setNotifyMessages(enabled: Boolean): Int

    @Query("UPDATE user_settings SET notifyFeedback = :enabled WHERE id = 1")
    suspend fun setNotifyFeedback(enabled: Boolean): Int

    @Query("UPDATE user_settings SET searchUserLanguage = :code WHERE id = 1")
    suspend fun setSearchUserLanguage(code: String): Int
}

