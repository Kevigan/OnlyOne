package com.example.onlyone.data

import com.google.firebase.Timestamp

data class AdminReportItem(
    val id: String,
    val status: String,
    val createdAt: Timestamp?,
    val reason: String?,
    val reporterId: String?,
    val reporterUsername: String?,
    val offenderId: String?,
    val offenderUsername: String?,
    val messageId: String?,
    val messagePreview: String?,
    val messageTimestamp: Timestamp?,
    val actionTaken: String?,

    // NEW — moderation counters
    val offenderWarnCount: Int? = null,
    val offenderBanCount: Int? = null,
    val offenderLastWarnedAt: Timestamp? = null,
    val offenderLastBannedAt: Timestamp? = null,
)
