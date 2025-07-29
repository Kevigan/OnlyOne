package com.example.onlyone.data

sealed class MessageResult {
    data class Success(
        val gold: Int,
        val points: Int,
        val rune: String? = null // null if no rune
    ) : MessageResult()

    object AlreadySent : MessageResult()
    object Error : MessageResult()
}

