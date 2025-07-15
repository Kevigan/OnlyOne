package com.example.onlyone.data

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderAvatarId: String = "",
    val senderMood: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val feedback: Int? = null
)


