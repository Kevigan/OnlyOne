package com.example.onlyone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.example.onlyone.views.FriendsView
import com.example.onlyone.views.LoginView
import com.example.onlyone.views.MainView
import com.example.onlyone.views.SettingsView
import com.example.onlyone.views.ShopView
import com.example.onlyone.views.SplashView

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                    startDestination = Screen.SplashScreen.route
                ) {
                    composable(Screen.SplashScreen.route) {
                        SplashView(
                            sessionViewModel = sessionViewModel,
                            navController = navController
                        )
                    }

                    composable(Screen.MainScreen.route) {
                        MainView(
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel
                        )
                    }

                    composable(Screen.FriendsScreen.route) {
                        FriendsView(userViewModel = userViewModel)
                    }

                    composable(Screen.ShopScreen.route) {
                        ShopView(userViewModel = userViewModel)
                    }

                    composable(Screen.SettingsScreen.route) {
                        SettingsView(userViewModel = userViewModel)
                    }

                    composable(Screen.LoginScreen.route) {
                        LoginView(
                            navController = navController,
                            sessionViewModel = sessionViewModel,
                            userViewModel = userViewModel
                        )
                    }

                }
            }
        }
    }
}


