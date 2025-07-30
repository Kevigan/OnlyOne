package com.example.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.onlyone.data.LocalUserSettings

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE uid = :uid LIMIT 1")
    suspend fun getSettings(uid: String): LocalUserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: LocalUserSettings)
}
