package com.example.onlyone.views.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.R
import com.example.onlyone.viewModels.userViewModel.AchievementManager

@Composable
fun AchievementCardGradient(
    achievement: AchievementManager.AchievementWithProgress,
    modifier: Modifier = Modifier,
    width: Dp = 160.dp,
    height: Dp = 200.dp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 2.dp,
    elevation: Dp = 6.dp
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD54F), Color.White) // amber-ish -> white
    )

    val progress = if (achievement.threshold > 0)
        (achievement.progress.toFloat() / achievement.threshold).coerceIn(0f, 1f)
    else 0f

    val trophyTint = if (achievement.completed) Color(0xFFFFD700) else Color.Gray

    Box(
        modifier = modifier
            .size(width, height)          // ✅ same size for all cards
            .shadow(elevation, shape, clip = false)
            .border(borderWidth, borderBrush, shape) // ✅ gradient border
            .clip(shape)
            .background(MaterialTheme.colors.surface)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents, // Trophy icon
                    contentDescription = stringResource(R.string.achv_title),
                    tint = trophyTint,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(achievement.name, style = MaterialTheme.typography.subtitle1)
                Text(achievement.description, style = MaterialTheme.typography.body2)
            }

            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(
                        R.string.achv_progress_xy,
                        achievement.progress,
                        achievement.threshold
                    ),
                    style = MaterialTheme.typography.caption
                )

                if (achievement.completed) {
                    Text(
                        stringResource(R.string.achv_unlocked),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

