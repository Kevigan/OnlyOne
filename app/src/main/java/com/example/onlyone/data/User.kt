package com.example.onlyone.data

import com.google.firebase.Timestamp

// 🔐 Private identity & settings (users_private/{uid})
data class UserPrivate(
    val uid: String = "",
    val email: String = "",
    val notifications: Map<String, Boolean> = mapOf(
        "message" to true,
        "feedback" to true
    ),
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,

    // 🔗 Social connections
    val friendList: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList()
)

// 🌍 Public-facing profile (users_public/{uid})
data class PublicUser(
    val uid: String = "",
    val username: String = "",
    val moodStatus: String = "",
    val chatLanguage: String = "en",
    val avatarId: Int = 0,
    val points: Int = 0
)

// ✨ Upgradeable user limits (users_upgrades/{uid} or nested in users_private)
data class UserUpgrades(
    val maxMessageLength: Int = 25,
    val maxMoments: Int = 75,
    val maxSwipes: Int = 50,
    val maxAdsPerDay: Int = 3
)

// 💰 Inventory (users_inventory/{uid})
data class UserInventory(
    val gold: Int = 0,
    val runes_rare: Int = 0,
    val runes_super_rare: Int = 0,
    val runes_mega_rare: Int = 0
)

// 🔁 Daily engagement state (engagement_status/{uid})
data class UserEngagementStatus(
    val uid: String = "",
    val swipesUsed: Int = 0,
    val momentsAvailable: Int = 75,
    val adsWatchedToday: Int = 0,
    val lastRefill: Timestamp? = null
)


data class UserComposite(
    val uid: String,
    val email: String,
    val username: String,
    val avatarId: Int,
    val moodStatus: String,
    val chatLanguage: String,
    val isPro: Boolean,
    val gold: Int,
    val points: Int,
    val runes_rare: Int,
    val runes_super_rare: Int,
    val runes_mega_rare: Int,
    val blockList: List<String>,
    val friendList: List<String>,
    val incomingFriendRequests: List<String>,
    val outgoingFriendRequests: List<String>,
    val maxMessageLength: Int,
    val notifications: Map<String, Boolean>,
    val reportCount: Int,
    val maxMoments: Int,
    val maxSwipes: Int,
    val maxAdsPerDay: Int
)
