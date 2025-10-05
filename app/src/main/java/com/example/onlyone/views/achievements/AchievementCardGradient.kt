package com.example.onlyone.views.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.userViewModel.AchievementManager

@Composable
fun AchievementCardGradient(
    achievement: AchievementManager.AchievementWithProgress,
    modifier: Modifier = Modifier,
    width: Dp = 160.dp,
    height: Dp = 200.dp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 2.dp,
    elevation: Dp = 6.dp,
    theme: ThemeTokens,
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD54F), Color.White)
    )

    val progress = if (achievement.threshold > 0)
        (achievement.progress.toFloat() / achievement.threshold).coerceIn(0f, 1f)
    else 0f

    val trophyTint = if (achievement.completed) Color(0xFFFFD700) else Color.Gray

    // Wrap Card with a gradient border; Card background is OPAQUE to mask overlays
    Box(
        modifier = modifier
            .size(width, height)
            .border(borderWidth, borderBrush, shape)   // gradient stroke outside
            .clip(shape)
    ) {
        Card(
            shape = shape,
            elevation = elevation,
            backgroundColor = MaterialTheme.colors.surface, // opaque → no inner “box”
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---- Header: icon + centered text ----
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = stringResource(R.string.achv_title),
                        tint = trophyTint,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.subtitle1,
                        color = theme.textColor,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.body2,
                        color = theme.textColor.copy(alpha = 0.9f),
                        maxLines = 3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---- Progress block: rounded track ----
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val trackShape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(trackShape)
                            .background(theme.textColor.copy(alpha = 0.10f)) // subtle track
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Color(0xFFFFD54F),
                            backgroundColor = Color.Transparent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(
                            R.string.achv_progress_xy,
                            achievement.progress,
                            achievement.threshold
                        ),
                        color = theme.textColor,
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (achievement.completed) {
                        Text(
                            stringResource(R.string.achv_unlocked),
                            color = theme.textColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
