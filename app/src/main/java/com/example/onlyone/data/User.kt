package com.example.onlyone.data

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val moodStatus: String = "",
    val points: Int = 0,
    val isPro: Boolean = false,
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,
    val avatarId: Int = 0,
    val friendList: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList(),
    val maxMessageLength: Int = 25, // ✅ new field
    val gold: Int = 0
)

data class PublicUser(
    val uid: String = "",
    val username: String = "",
    val moodStatus: String = "",
    val avatarId: Int = 0,
    val points: Int = 0
)

data class PrivateUser(
    val uid: String = "",
    val email: String = "",
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,
    val isPro: Boolean = false,
    val friendList: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList(),
    val fcmToken: String? = null,
    val maxMessageLength: Int = 25,
    val gold: Int = 0
)

data class UserSwipeStatus(
    val uid: String = "",
    val swipesUsed: Int = 0,
    val swipesGranted: Int = 25,
    val lastReset: Timestamp? = null
)

