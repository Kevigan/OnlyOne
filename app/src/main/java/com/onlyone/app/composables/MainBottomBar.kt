package com.onlyone.app.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.Screen
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens


@Composable
fun MainBottomBar(
    navController: NavController,
    currentRoute: String?,
    theme: ThemeTokens,
    showFriendsPlus: Boolean = false
) {
    val navBarHeight = 32.dp
    var menuExpanded by remember { mutableStateOf(false) }

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
                    //.graphicsLayer { alpha = 0.6f }   // applies to whole Box background
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                theme.gradientColor1,
                                theme.gradientColor2
                            ),
                            start = Offset(0f, Float.POSITIVE_INFINITY),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
            {
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
                            onClick = { navController.navigate(Screen.FriendsScreen.route) },
                            badge = if (showFriendsPlus) {
                                { PlusBadge() }
                            } else null
                        )

                        Spacer(modifier = Modifier.width(64.dp))

                        NavigationIcon(
                            icon = null, // set to null, since we’ll use a painter instead
                            contentDescription = "Shop",
                            isSelected = currentRoute == Screen.ShopScreen.route,
                            onClick = { navController.navigate(Screen.ShopScreen.route) },
                            customPainter = painterResource(id = R.drawable.baseline_hotel_class_24),
                            /*selectedGradient = Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFD54F), // amber 300
                                    Color(0xFFFFB300)  // amber 600
                                )
                            )*/
                        )
                        NavigationIcon(
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            isSelected = currentRoute == Screen.SettingsScreen.route,
                            onClick = { navController.navigate(Screen.SettingsScreen.route) }
                        )
                    }
                }
            }
        }

        // ⬇️ Centered floating cluster — two big FABs, right one expandable.
// Place this AFTER the Surface block so it renders on top.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)   // ✅ center the whole group
                .offset(y = (-36).dp)         // ✅ float above the bar (same as original)
                .wrapContentSize()            // ✅ group takes only what it needs
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Left: Write message (same size as before)
                FloatingRoundButton(
                    onClick = {
                        navController.navigate(
                            Screen.ChatScreen.createRoute(
                                "none",
                                true
                            )
                        )
                    },
                    painter = painterResource(id = R.drawable.baseline_message_24),
                    gradient = Brush.linearGradient(
                        listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                    )
                )
                ActivitiesExpandableFab(
                    navController = navController,
                )
            }
        }

    }
}

@Composable
fun NavigationIcon(
    icon: ImageVector? = null,
    customPainter: Painter? = null,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconSize: Dp = 34.dp,
    selectedGradient: Brush = Brush.linearGradient(
        listOf(Color(0xFF8A226D), Color(0xFFEB69CD))
    ),
    backgroundColor: Color = MaterialTheme.colors.onBackground,
    badge: (@Composable () -> Unit)? = null
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(45.dp) // container for the icon + badge
                .background(
                    brush = if (isSelected) selectedGradient
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (customPainter != null) {
                Icon(
                    painter = customPainter,
                    contentDescription = contentDescription,
                    tint = backgroundColor,
                    modifier = Modifier.size(iconSize)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = backgroundColor,
                    modifier = Modifier.size(iconSize)
                )
            }

            // ⬇ Badge sits half on/half off the icon (top-right)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (-6).dp)
                ) { badge() }
            }
        }
    }
}

@Composable
fun PlusBadge() {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(Color(0xFF2E7D32), CircleShape) // green-ish
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
    }
}

@Composable
private fun FloatingRoundButton(
    onClick: () -> Unit,
    painter: Painter,
    gradient: Brush,
    size: Dp = 62.dp,
    iconSize: Dp = 36.dp
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        modifier = Modifier.size(size),
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
                .background(brush = gradient, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun MiniFloatingButton(
    onClick: () -> Unit,
    painter: Painter,
    gradient: Brush,
) {
    FloatingRoundButton(
        onClick = onClick,
        painter = painter,
        gradient = gradient,
        size = 48.dp,
        iconSize = 24.dp
    )
}

@Composable
private fun ActivitiesExpandableFab(
    navController: NavController,
) {
    Box(modifier = Modifier.size(62.dp)) {
        var menuExpanded by remember { mutableStateOf(false) }

        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
            exit  = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentWidth(unbounded = true)
                .offset(y = (-64).dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Create Chain
                MiniFloatingButton(
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Screen.CreateChainMessageScreen.route)
                    },
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    //contentDescription = stringResource(R.string.cd_start_chain),         // ←
                    gradient = Brush.linearGradient(listOf(Color(0xFF3949AB), Color(0xFF7E57C2)))
                )

                // Open Chains List
                MiniFloatingButton(
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Screen.ChainsScreen.route)
                    },
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    gradient = Brush.linearGradient(listOf(Color(0xFF3949AB), Color(0xFF7E57C2)))
                )

                // Saved Chains
                MiniFloatingButton(
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Screen.SavedChainsScreen.route)
                    },
                    painter = painterResource(id = R.drawable.baseline_bookmarks_24),
                    gradient = Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)))
                )
            }
        }

        FloatingRoundButton(
            onClick = { menuExpanded = !menuExpanded },
            painter = painterResource(id = R.drawable.baseline_hotel_class_24),
            gradient = Brush.linearGradient(listOf(Color(0xFF5C6BC0), Color(0xFF9575CD))),
            size = 62.dp,
            iconSize = 36.dp
        )
    }
}










