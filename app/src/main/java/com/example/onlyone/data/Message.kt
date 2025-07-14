package com.example.onlyone.data

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",  // optional but useful
    val senderAvatarId: String = "",  // helps avoid lookup
    val senderMood: String = "",      // snapshot of mood at time of message
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val feedback: Int? = null
)


