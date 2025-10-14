package com.onlyone.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun buildMessageId(senderId: String, receiverId: String): String {
    val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    utcFormat.timeZone = TimeZone.getTimeZone("UTC") // ✅ Force UTC
    val todayUtc = utcFormat.format(Date())

    return "${senderId}_${receiverId}_$todayUtc"
}

