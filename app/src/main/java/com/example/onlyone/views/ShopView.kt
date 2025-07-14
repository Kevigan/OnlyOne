package com.example.onlyone.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay

@Composable
fun ShopView() {
    // Fake image lists – replace with actual drawable resources
    val avatarImages = listOf(
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
        R.drawable.baseline_tag_faces_24,
        R.drawable.baseline_sentiment_neutral_24,
    )

    val moodImages = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔷 Shop Title
            Text(
                text = "Shop",
                style = MaterialTheme.typography.h5,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🔷 Coins + Value
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coins",
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    modifier = Modifier.padding(end = 8.dp, bottom = 16.dp)
                )

                Text(
                    text = "9999$",
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // 🟣 Avatars section
        Text(
            text = "Avatars",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
        )
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 21),
            overlayColor = Color.Gray,
            onDismiss = {},
            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            paddingBox2 = PaddingValues(6.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(avatarImages) { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🟡 Moods section
        Text(
            text = "Moods",
            style = MaterialTheme.typography.h6,
            color = Color.White,
            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
        )
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 21),
            overlayColor = Color.Gray,
            onDismiss = {},
            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            paddingBox2 = PaddingValues(6.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moodImages) { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Mood",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

