package com.example.onlyone.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.onlyone.composables.CustomColorOverlay
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainView() {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colors.background

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp, start = 12.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Top Row with text on left, icon on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.h6,
                color = Color.White
            )

            IconButton(onClick = { /* handle icon click */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // CustomColorOverlay (wrap content)
        CustomColorOverlay(modifier = Modifier.wrapContentSize(), onDismiss = {}) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Overlay Content", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* action */ }) {
                    Text("Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CustomColorOverlay (fill width)
        CustomColorOverlay(modifier = Modifier.fillMaxWidth(), onDismiss = {}) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Overlay Content", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* action */ }) {
                    Text("Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable list overlay
        CustomColorOverlay(modifier = Modifier.fillMaxWidth(), onDismiss = {}) {
            Column(modifier = Modifier.height(300.dp)) { // constrain height to enable scrolling
                Text("User List", style = MaterialTheme.typography.h6, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(listOf("Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "Grace", "Hannah", "Isaac", "Julia", "Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "Grace", "Hannah", "Isaac", "Julia")) { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.body1,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}



