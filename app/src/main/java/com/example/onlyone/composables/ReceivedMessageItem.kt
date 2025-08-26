package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.R

@Composable
fun ReceivedMessageItem(
    avatarResId: Int,
    name: String,
    message: String,
    expiration: String,
    isRead: Boolean,
    feedback: Int,
    onClick: () -> Unit
) {
    val borderColor = if (isRead) Color.White else Color(0xFF6FCF97) // greenish when unread
    val borderWidth = if(isRead) 0.1.dp else 2.dp
    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(percent = 45),
        overlayColor = Color.Gray,
        borderColor = borderColor, // ✅ use dynamic color
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp),
        borderWidth = borderWidth
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            // 🔷 Top Row: Avatar + Name + Reactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Avatar for $name",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = name,
                        style = MaterialTheme.typography.body1,
                        color = Color.White
                    )
                }

                val (iconRes, tint, description) = when (feedback) {
                    1 -> Triple(R.drawable.baseline_thumb_up_off_alt_24, Color.Green, "You gave thumbs up")
                    0 -> Triple(R.drawable.baseline_sentiment_neutral_24, Color.Gray, "You gave neutral feedback")
                    -1 -> Triple(R.drawable.baseline_thumb_down_off_alt_24, Color.Red, "You gave thumbs down")
                    else -> Triple(R.drawable.baseline_thumb_up_off_alt_24, Color.White, "No feedback given")
                }

                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = description,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 🔷 Bottom Row: Message + Expiration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.body2,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(1.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(R.string.main_exp_in_label),
                        fontSize = 8.sp,
                        style = MaterialTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = expiration,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.body2,
                        color = Color.White
                    )
                }
            }
        }
    }
}

