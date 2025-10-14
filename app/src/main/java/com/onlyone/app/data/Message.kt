package com.onlyone.app.data

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderAvatarId: Int = 0,
    val senderMood: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val read: Boolean = false,
    val feedback: Int? = null
)


