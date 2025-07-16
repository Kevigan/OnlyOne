package com.example.onlyone.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "", // ✅ Private
    val moodStatus: String = "",
    val points: Int = 0,
    val isPro: Boolean = false,
    val blockList: List<String> = emptyList(), // ✅ Private
    val reportCount: Int = 0, // ✅ Private
    val avatarId: Int = 0,
    val friendList: List<String> = emptyList(), // ✅ Private
    val incomingFriendRequests: List<String> = emptyList(), // ✅ Private
    val outgoingFriendRequests: List<String> = emptyList()  // ✅ Private
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
    val outgoingFriendRequests: List<String> = emptyList()
)

