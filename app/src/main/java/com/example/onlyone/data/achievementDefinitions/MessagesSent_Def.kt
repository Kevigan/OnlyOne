package com.example.onlyone.data.achievementDefinitions

object MessageAchievements {

    val definitions = listOf(
        mapOf(
            "id" to "send_1",
            "name" to "First Message",
            "description" to "Send your very first message.",
            "threshold" to 1,
            "type" to "messagesSent",
            "points" to 5,
            "rarity" to "common",
            "icon" to "✉️"
        ),
        mapOf(
            "id" to "send_10",
            "name" to "Getting Warm",
            "description" to "Send 10 messages.",
            "threshold" to 10,
            "type" to "messagesSent",
            "points" to 10,
            "rarity" to "common",
            "icon" to "📨"
        ),
        mapOf(
            "id" to "send_25",
            "name" to "Warming Up",
            "description" to "Send 25 messages.",
            "threshold" to 25,
            "type" to "messagesSent",
            "points" to 15,
            "rarity" to "common",
            "icon" to "💬"
        ),
        mapOf(
            "id" to "send_50",
            "name" to "Chatterbox",
            "description" to "Send 50 messages.",
            "threshold" to 50,
            "type" to "messagesSent",
            "points" to 20,
            "rarity" to "uncommon",
            "icon" to "🗨️"
        ),
        mapOf(
            "id" to "send_100",
            "name" to "Talking Machine",
            "description" to "Send 100 messages.",
            "threshold" to 100,
            "type" to "messagesSent",
            "points" to 30,
            "rarity" to "uncommon",
            "icon" to "🧠"
        ),
        mapOf(
            "id" to "send_250",
            "name" to "Message Master",
            "description" to "Send 250 messages.",
            "threshold" to 250,
            "type" to "messagesSent",
            "points" to 40,
            "rarity" to "rare",
            "icon" to "📣"
        ),
        mapOf(
            "id" to "send_500",
            "name" to "Messenger Supreme",
            "description" to "Send 500 messages.",
            "threshold" to 500,
            "type" to "messagesSent",
            "points" to 60,
            "rarity" to "epic",
            "icon" to "📬"
        ),
        mapOf(
            "id" to "send_1000",
            "name" to "Legendary Voice",
            "description" to "Send 1000 messages.",
            "threshold" to 1000,
            "type" to "messagesSent",
            "points" to 100,
            "rarity" to "legendary",
            "icon" to "🎤"
        )
    )
}
