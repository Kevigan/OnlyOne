package com.example.onlyone.data

import com.google.firebase.Timestamp

// 🔐 Private (users_private/{uid})
data class UserPrivate(
    val uid: String = "",
    val email: String = "",
    val notifications: Map<String, Boolean> = mapOf(
        "message" to true,
        "feedback" to true
    ),
    val blockList: List<String> = emptyList(),
    val reportCount: Int = 0,
    val friendList: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList(),

    // 🆕 New
    val isVerified: Boolean = false
)

// ⭐ Favourite message (nested in users_public)
data class FavouriteMessage(
    val text: String = "",
    val fromUid: String = "",
    val messageId: String = "",
    val chosenAt: Timestamp? = null
)

// 🌍 Public (users_public/{uid})
data class PublicUser(
    val uid: String = "",
    val username: String = "",
    val moodStatus: String = "",
    val chatLanguage: String = "en",
    val avatarId: Int = 0,
    val moodId: Int = 0,
    val points: Int = 0,
    val achievementCount: Int = 0,
    val favouriteMessage: FavouriteMessage? = null,

    // 🆕 New
    val gender: String = "unspecified",
    val age: Int? = null,
    val city: String = ""
)

// ✨ Upgrades (users_upgrades/{uid})
data class UserUpgrades(
    val maxMessageLength: Int = 25,
    val maxMoodLength: Int = 25,
    val maxMoments: Int = 75,
    val maxSwipes: Int = 50,
    val maxAdsPerDay: Int = 3
)

// 💰 Inventory (users_inventory/{uid})
data class UserInventory(
    val gold: Int = 0,
    val runes_rare: Int = 0,
    val runes_super_rare: Int = 0,
    val runes_mega_rare: Int = 0,
    val ownedAvatars: List<Int> = emptyList(),
    val ownedMoods: List<Int> = emptyList(),
    val ownedThemes: List<Int> = emptyList()
)

// 🔁 Engagement (engagement_status/{uid})
data class UserEngagementStatus(
    val uid: String = "",
    val swipesUsed: Int = 0,
    val momentsAvailable: Int = 75,
    val adsWatchedToday: Int = 0,
    val lastRefill: Timestamp? = null
)

// 🧩 Merged user from callable getUserWithFriends
data class UserComposite(
    val uid: String,
    val email: String,
    val username: String,
    val avatarId: Int,
    val moodId: Int,
    val moodStatus: String,
    val chatLanguage: String,
    val isPro: Boolean,

    // Inventory
    val gold: Int,
    val points: Int,
    val runes_rare: Int,
    val runes_super_rare: Int,
    val runes_mega_rare: Int,
    val ownedAvatars: List<Int>,
    val ownedMoods: List<Int>,
    val ownedThemes: List<Int>,

    // Social
    val blockList: List<String>,
    val friendList: List<String>,
    val incomingFriendRequests: List<String>,
    val outgoingFriendRequests: List<String>,
    val notifications: Map<String, Boolean>,
    val reportCount: Int,

    // Upgrades
    val maxMoments: Int,
    val maxSwipes: Int,
    val maxAdsPerDay: Int,
    val maxMessageLength: Int,
    val maxMoodLength: Int,

    // Extras from users_public
    val achievementCount: Int,
    val favouriteMessage: FavouriteMessage?,
    val gender: String,
    val age: Int?,
    val city: String,

    // 🆕 From users_private
    val isVerified: Boolean
)

