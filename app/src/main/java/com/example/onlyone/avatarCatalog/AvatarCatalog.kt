package com.example.onlyone.avatarCatalog

import com.example.onlyone.R

object AvatarCatalog {
    val avatars = listOf(
        AvatarItem(id = 1, imageRes = R.drawable.ghosthead_happy, cost = 100),
        AvatarItem(id = 2, imageRes = R.drawable.ghosthead_angry, cost = 150),
        AvatarItem(id = 3, imageRes = R.drawable.ghosthead_sad, cost = 300),
        AvatarItem(id = 4, imageRes = R.drawable.ghosthead, cost = 500),
        // Add more avatars here
    )
}
data class AvatarItem(
    val id: Int,
    val imageRes: Int,
    val cost: Int
)
