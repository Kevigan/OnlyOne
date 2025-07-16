package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun FriendItem(
    avatarResId: Int,
    name: String,
    status: String
) {
    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 45),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(vertical = 1.dp),
        paddingBox2 = PaddingValues(vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 👤 Avatar
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Avatar for $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Status
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.body1,
                    color = Color.White
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
