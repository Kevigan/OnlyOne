package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalFavoriteMessage(
    @PrimaryKey val id: String,           // use original message id to avoid dupes
    val senderId: String,
    val senderUsername: String,
    val senderAvatarId: Int,
    val senderMood: String,
    val receiverId: String,
    val content: String,
    val originalTimestamp: Long,          // when the message was sent
    val savedAt: Long                     // when the user saved it
)

