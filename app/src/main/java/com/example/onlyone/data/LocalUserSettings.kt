package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class LocalUserSettings(
    @PrimaryKey val id: Int = 1,
    val language: String = "en",                  // app language (local only)
    val notifyMessages: Boolean = true,           // 🔔 local toggle for message notifications
    val notifyFeedback: Boolean = true,          // 🔔 local toggle for feedback notifications
    val searchUserLanguage: String = "any"       // 🔎 user discovery language filter
)

