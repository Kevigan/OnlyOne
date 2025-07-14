package com.example.onlyone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.onlyone.composables.MainBottomBar
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.example.onlyone.views.FriendsView
import com.example.onlyone.views.MainView
import com.example.onlyone.views.SettingsView
import com.example.onlyone.views.ShopView

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ⬇️ OUTER BOX with gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.background,
                        Color(0xFF230C36)
                    )
                )
            )
    ) {
        Scaffold(
            bottomBar = {
                MainBottomBar(navController = navController, currentRoute = currentRoute)
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.MainScreen.route
                ) {
                    composable(Screen.MainScreen.route) {
                        MainView()
                    }

                    composable(Screen.FriendsScreen.route) {
                        FriendsView()
                    }

                    composable(Screen.ShopScreen.route) {
                        ShopView()
                    }

                    composable(Screen.SettingsScreen.route) {
                        SettingsView()
                    }
                }
            }
        }
    }
}

