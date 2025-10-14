package com.onlyone.app.data.chainMessage

import com.google.firebase.Timestamp

data class ChainStep(
    val index: Int = -1,
    val authorUid: String = "",
    val authorName: String? = null,
    val text: String = "",
    val createdAt: Timestamp? = null
)
