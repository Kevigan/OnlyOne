package com.onlyone.app.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainTopBar(onActionClick: () -> Unit) {
    val topBarHeight = 64.dp
    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
            .border(
                width = 0.1.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF433A52),
                                Color(0xFF5A4A6A)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onActionClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

