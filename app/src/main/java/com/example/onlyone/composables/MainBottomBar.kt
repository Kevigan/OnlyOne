package com.example.onlyone.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainBottomBar(navController: NavController) {
    val navBarHeight = 32.dp

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp + navBarHeight),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = 8.dp,
            color = MaterialTheme.colors.primary
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 12.dp), // spacing to edge
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leftmost icon
                    IconButton(onClick = { /* TODO: Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Inner-left
                    IconButton(onClick = { /* TODO: Home */ }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(64.dp)) // space for floating button

                    // Inner-right
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Rightmost icon
                    IconButton(onClick = { /* TODO: Info */ }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Floating round button at center top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
        ) {
            Button(
                onClick = { /* Center action */ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface),
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colors.onSecondary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
