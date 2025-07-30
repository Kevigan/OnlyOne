package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class LocalUserSettings(
    @PrimaryKey val uid: String,
    val language: String = "en",             // app language (local only)
    val notifyMessages: Boolean = true,      // 🔔 local toggle for message notifications
    val notifyFeedback: Boolean = true       // 🔔 local toggle for feedback notifications
)

