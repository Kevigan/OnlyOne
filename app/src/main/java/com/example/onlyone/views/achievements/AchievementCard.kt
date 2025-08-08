package com.example.onlyone.views.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.viewModels.userViewModel.AchievementManager

@Composable
fun AchievementCard(achievement: AchievementManager.AchievementWithProgress) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(achievement.icon, fontSize = 28.sp)
            Text(achievement.name, style = MaterialTheme.typography.subtitle1)
            Text(achievement.description, style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = achievement.progress.toFloat() / achievement.threshold,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${achievement.progress} / ${achievement.threshold}")
            Text("ID: ${achievement.id}")
            Text("Type: ${achievement.type}")
            Text("Completed: ${achievement.completed}")
            Text("Progress: ${achievement.progress}/${achievement.threshold}")

            if (achievement.completed) {
                Text("✅ Unlocked", color = Color.Green, fontWeight = FontWeight.Bold)
            }
        }
    }
}
