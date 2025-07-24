package com.example.onlyone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalSwipeStatus(
    @PrimaryKey val uid: String,
    val swipesUsed: Int,
    val swipesGranted: Int
)

