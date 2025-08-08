package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    messagesLeft: Int,
    points: String,
    pointsRank: Int,
    gold: Int,
    runesRare: Int,
    runesSuperRare: Int,
    runesMegaRare: Int,
    millisUntilReset: Long,
    onAchievementsClick: () -> Unit // ✅ Callback
) {
    val hours = (millisUntilReset / 1000) / 3600
    val minutes = ((millisUntilReset / 1000) % 3600) / 60

    val runeList = listOfNotNull(
        if (runesRare > 0) "$runesRare rare" else null,
        if (runesSuperRare > 0) "$runesSuperRare super" else null,
        if (runesMegaRare > 0) "$runesMegaRare mega" else null
    )

    // ✅ Wrap entire content in a Row to push IconButton to the right
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // 🔹 Main stats column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔹 Line 1: Reset time
            Text(
                text = "Reset in: ${hours}h ${minutes}m UTC",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            // 🔹 Line 2: Gold, Points, Runes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gold: $gold", color = Color.White)
                    Text("Points: $points", color = Color.White)
                    Text("Rank: ${getUserRank(pointsRank)}", color = Color.White)
                }

                if (runeList.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Runes:", color = Color.White)
                        runeList.forEach { rune ->
                            Text(rune, color = Color.White)
                        }
                    }
                }
            }
        }

        // ✅ IconButton to the right
        IconButton(
            onClick = onAchievementsClick,
            modifier = Modifier
                .size(48.dp)
                .padding(start = 8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Star, // or use Icons.Outlined.EmojiEvents
                contentDescription = "Achievements",
                tint = Color.White
            )
        }
    }
}


fun getUserRank(points: Int): String {
    return when {
        points >= 1050 -> "S-Class"
        points >= 900 -> "A-Class 2"
        points >= 750 -> "A-Class 3"
        points >= 600 -> "B-Class 1"
        points >= 450 -> "B-Class 2"
        points >= 300 -> "B-Class 3"
        points >= 200 -> "C-Class 1"
        points >= 100 -> "C-Class 2"
        points >= 50 -> "C-Class 3"
        else -> "Trainee"
    }
}





