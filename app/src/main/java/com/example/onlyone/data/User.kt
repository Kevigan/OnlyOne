package com.example.onlyone.data

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val moodStatus: String = "",
    val points: Int = 0,
    val isPro: Boolean = false,
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,
    val avatarId: Int = 0
)

