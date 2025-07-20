package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalFriend(
    @PrimaryKey val uid: String,
    val username: String,
    val moodStatus: String,
    val avatarId: Int,
    val points: Int
)

