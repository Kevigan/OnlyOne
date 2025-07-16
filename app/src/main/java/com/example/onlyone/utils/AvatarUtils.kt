package com.example.onlyone.composables

import com.example.onlyone.R

fun mapAvatarIdToDrawable(avatarId: Int): Int {
    return when (avatarId) {
        0 -> R.drawable.ghosthead
        1 -> R.drawable.ghosthead_angry
        2 -> R.drawable.ghosthead_happy
        3 -> R.drawable.ghosthead_sad
        // Add as many as you support
        else -> R.drawable.ghosthead // Fallback avatar
    }
}


