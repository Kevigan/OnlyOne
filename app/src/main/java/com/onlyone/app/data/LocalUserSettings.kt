package com.onlyone.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class LocalUserSettings(
    @PrimaryKey val id: Int = 1,

    val language: String = "en",                 // 🌐 app language (local only)

    val notifyMessages: Boolean = true,          // 🔔 show message notifications
    val notifyFeedback: Boolean = true,          // 🔔 show feedback/reaction notifications
    val notifyFriendRequests: Boolean = true,    // 🧩 show friend request notifications (NEW)

    val searchUserLanguage: String = "any"       // 🔎 user discovery language filter
)

