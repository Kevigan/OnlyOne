package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserStatsCardContent(
    messagesLeft: String,
    dailyPoints: String,
    pointsBank: String,
    rank: String,
    avatarResId: Int
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🟢 Left column: stats
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Messages left: $messagesLeft",
                style = MaterialTheme.typography.body1,
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = "Daily points earned: $dailyPoints",
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
            Text(
                text = "Points in bank: $pointsBank",
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
            Text(
                text = "Rank: $rank",
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
        }

        // 🟣 Right column: avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Current Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
        }
    }
}
