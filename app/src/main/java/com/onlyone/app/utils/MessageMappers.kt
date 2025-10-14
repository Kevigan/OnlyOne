package com.onlyone.app.utils

import com.google.firebase.firestore.FieldValue
import com.onlyone.app.data.Message

fun Message.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "senderId" to senderId,
    "senderUsername" to senderUsername,
    "senderAvatarId" to senderAvatarId,
    "senderMood" to senderMood,
    "receiverId" to receiverId,
    "content" to content,
    "timestamp" to (timestamp ?: FieldValue.serverTimestamp()), // ✅ Secure timestamp
    "read" to read,
    "feedback" to feedback
)
