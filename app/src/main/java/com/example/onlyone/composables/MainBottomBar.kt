package com.example.onlyone.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen

@Composable
fun MainBottomBar(navController: NavController, currentRoute: String?) {
    val navBarHeight = 32.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp + navBarHeight)
                .border(
                    width = 0.1.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(32.dp)
                ), // ✅ Border here
            shape = RoundedCornerShape(32.dp),
            elevation = 8.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF353535).copy(alpha = 0.9f), // Darker
                                Color(0xFF1F1F1F).copy(alpha = 0.9f)  // Lighter
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                // Your Column or Row content goes here
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 12.dp), // inner content spacing
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationIcon(
                            icon = Icons.Default.Home,
                            contentDescription = "Home",
                            isSelected = currentRoute == Screen.MainScreen.route,
                            onClick = { navController.navigate(Screen.MainScreen.route) }
                        )

                        NavigationIcon(
                            icon = Icons.Default.Person,
                            contentDescription = "Friends",
                            isSelected = currentRoute == Screen.FriendsScreen.route,
                            onClick = { navController.navigate(Screen.FriendsScreen.route) }
                        )

                        Spacer(modifier = Modifier.width(64.dp))

                        NavigationIcon(
                            icon = Icons.Default.Settings,
                            contentDescription = "Shop",
                            isSelected = currentRoute == Screen.ShopScreen.route,
                            onClick = { navController.navigate(Screen.ShopScreen.route) }
                        )

                        NavigationIcon(
                            icon = Icons.Default.Info,
                            contentDescription = "Settings",
                            isSelected = currentRoute == Screen.SettingsScreen.route,
                            onClick = { navController.navigate(Screen.SettingsScreen.route) }
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
                onClick = { navController.navigate(Screen.ChatScreen.createRoute("none", true)) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent), // Transparent to show gradient inside
                modifier = Modifier.size(72.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 16.dp,
                    hoveredElevation = 10.dp,
                    focusedElevation = 10.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50), // green
                                    Color(0xFF81C784)  // lighter green
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_message_24),
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationIcon(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconSize: Dp = 34.dp,
    selectedGradient: Brush = Brush.linearGradient(
        listOf(Color(0xFF8A226D), Color(0xFFEB69CD))
    ),
    backgroundColor: Color = MaterialTheme.colors.onBackground
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    brush = if (isSelected) selectedGradient
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = backgroundColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
