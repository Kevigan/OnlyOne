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
    onClick: () -> Unit // ← new
) {
    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(percent = 45),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp),
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
                    // 👤 Avatar
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* thumbs up */ }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_thumb_up_off_alt_24),
                            contentDescription = "Thumbs up",
                            tint = Color.White
                        )
                    }
                }
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
                    fontSize = 16.sp, // 👈 custom font size
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
                        text = "exp. in",
                        fontSize = 8.sp, // 👈 smaller font
                        style = MaterialTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = expiration,
                        fontSize = 10.sp, // 👈 slightly bigger for emphasis
                        style = MaterialTheme.typography.body2,
                        color = Color.White
                    )
                }
            }
        }
    }
}

