package com.onlyone.app.utils

fun formatTimeLeft(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val expirationMillis = timestampMillis + 24 * 60 * 60 * 1000
    val timeLeftMillis = expirationMillis - now

    if (timeLeftMillis <= 0) return "Expired"

    val hours = (timeLeftMillis / (1000 * 60 * 60)).toInt()
    val minutes = ((timeLeftMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()

    return when {
        hours > 0 -> "$hours h ${minutes} m"
        minutes > 0 -> "$minutes min"
        else -> "1 min"
    }
}
