package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WrittenTodayEntity(
    @PrimaryKey val receiverId: String,
    val timestamp: Long
)

