package com.example.onlyone.views.shopView

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.composables.CustomColorOverlay

@Composable
fun MoodsSection(moodImages: List<Int>) {
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
