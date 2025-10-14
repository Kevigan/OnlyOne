package com.onlyone.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalMessage(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderUsername: String,
    val senderAvatarId: Int,
    val senderMood: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val read: Boolean,
    val feedback: Int?
)
