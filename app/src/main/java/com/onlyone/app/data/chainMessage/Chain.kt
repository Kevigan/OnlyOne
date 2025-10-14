package com.onlyone.app.data.chainMessage


data class Chain(
    val id: String = "",
    val ownerUid: String = "",
    val title: String? = null,
    val targetLength: Int = 0,
    val createdAt: com.google.firebase.Timestamp? = null,
    val expiresAt: com.google.firebase.Timestamp? = null,
    val deleteAt: com.google.firebase.Timestamp? = null,   // ← NEW (optional in old docs)
    val state: String = "open",                 // "open" | "complete" | "expired"
    val participants: List<String> = emptyList(),
    val currentAssignee: String? = null,
    val hiddenHistory: Boolean = false,
    val lastAssignAt: com.google.firebase.Timestamp? = null,
    val lastStepIndex: Int = -1,
    val lastContributorUid: String? = null,
    val participantsNames: Map<String, String> = emptyMap()
)

