package com.example.onlyone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.onlyone.composables.MainBottomBar
import com.example.onlyone.views.MainView

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            MainBottomBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier
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

                // Add other screens here (example)
                // composable(Screen.FriendsScreen.route) { FriendsView() }
            }
        }
    }
}
