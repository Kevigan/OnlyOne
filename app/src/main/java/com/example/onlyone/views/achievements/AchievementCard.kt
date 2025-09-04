package com.example.onlyone.views.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.userViewModel.AchievementManager

@Composable
fun AchievementCard(achievement: AchievementManager.AchievementWithProgress, theme: ThemeTokens) {
    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // keep your card breathing room
        shape = RoundedCornerShape(percent = 20),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(vertical = 1.dp),
        paddingBox2 = PaddingValues(vertical = 1.dp),
        theme = theme,
    ) {
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

                val progress = if (achievement.threshold > 0)
                    (achievement.progress.toFloat() / achievement.threshold).coerceIn(0f, 1f)
                else 0f

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    stringResource(
                        R.string.achv_progress_xy,
                        achievement.progress,
                        achievement.threshold
                    )
                )

                if (achievement.completed) {
                    Text(
                        stringResource(R.string.achv_unlocked),
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

