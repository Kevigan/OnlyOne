package com.example.onlyone.data

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val moodStatus: String = "",
    val chatLanguage: String = "en",
    val points: Int = 0,
    val isPro: Boolean = false,
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,
    val avatarId: Int = 0,
    val friendList: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList(),
    val maxMessageLength: Int = 25,
    val gold: Int = 0,

    // 🧙 New Rune Fields
    val runes_rare: Int = 0,
    val runes_super_rare: Int = 0,
    val runes_mega_rare: Int = 0,

    val notifications: Map<String, Boolean> = mapOf(
        "message" to true,
        "feedback" to true
    )
)

data class PublicUser(
    val uid: String = "",
    val username: String = "",
    val moodStatus: String = "",
    val chatLanguage: String = "en",
    val avatarId: Int = 0,
    val points: Int = 0
)

data class UserSwipeStatus(
    val uid: String = "",
    val swipesUsed: Int = 0,
    val swipesGranted: Int = 25,
    val lastReset: Timestamp? = null
)

