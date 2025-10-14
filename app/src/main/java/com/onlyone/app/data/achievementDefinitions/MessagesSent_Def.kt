package com.onlyone.app.data.achievementDefinitions

import android.content.Context
import com.onlyone.app.R

object MessageAchievements {

    fun definitions (context: Context) = listOf(
        mapOf(
            "id" to "send_1",
            "name" to context.getString(R.string.achv_send_1_name),
            "description" to context.getString(R.string.achv_send_1_desc),
            "threshold" to 1,
            "type" to "messagesSent",
            "points" to 5,
            "rarity" to "common",
            "icon" to "✉️"
        ),
        mapOf(
            "id" to "send_10",
            "name" to context.getString(R.string.achv_send_10_name),
            "description" to context.getString(R.string.achv_send_10_desc),
            "threshold" to 10,
            "type" to "messagesSent",
            "points" to 10,
            "rarity" to "common",
            "icon" to "📨"
        ),
        mapOf(
            "id" to "send_25",
            "name" to context.getString(R.string.achv_send_25_name),
            "description" to context.getString(R.string.achv_send_25_desc),
            "threshold" to 25,
            "type" to "messagesSent",
            "points" to 15,
            "rarity" to "common",
            "icon" to "💬"
        ),
        mapOf(
            "id" to "send_50",
            "name" to context.getString(R.string.achv_send_50_name),
            "description" to context.getString(R.string.achv_send_50_desc),
            "threshold" to 50,
            "type" to "messagesSent",
            "points" to 20,
            "rarity" to "uncommon",
            "icon" to "🗨️"
        ),
        mapOf(
            "id" to "send_100",
            "name" to context.getString(R.string.achv_send_100_name),
            "description" to context.getString(R.string.achv_send_100_desc),
            "threshold" to 100,
            "type" to "messagesSent",
            "points" to 30,
            "rarity" to "uncommon",
            "icon" to "🧠"
        ),
        mapOf(
            "id" to "send_250",
            "name" to context.getString(R.string.achv_send_250_name),
            "description" to context.getString(R.string.achv_send_250_desc),
            "threshold" to 250,
            "type" to "messagesSent",
            "points" to 40,
            "rarity" to "rare",
            "icon" to "📣"
        ),
        mapOf(
            "id" to "send_500",
            "name" to context.getString(R.string.achv_send_500_name),
            "description" to context.getString(R.string.achv_send_500_desc),
            "threshold" to 500,
            "type" to "messagesSent",
            "points" to 60,
            "rarity" to "epic",
            "icon" to "📬"
        ),
        mapOf(
            "id" to "send_1000",
            "name" to context.getString(R.string.achv_send_1000_name),
            "description" to context.getString(R.string.achv_send_1000_desc),
            "threshold" to 1000,
            "type" to "messagesSent",
            "points" to 100,
            "rarity" to "legendary",
            "icon" to "🎤"
        )
    )
}

