package com.example.onlyone

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.onlyone.cloudMessaging.MessageNotifier
import com.example.onlyone.cloudMessaging.RequestNotificationPermission
import com.example.onlyone.composables.ChatScreenEntry
import com.example.onlyone.composables.MainBottomBar
import com.example.onlyone.composables.MainTopBar
import com.example.onlyone.composables.ReceivedMessageItem
import com.example.onlyone.composables.TopSnackbar
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.example.onlyone.views.FriendsView
import com.example.onlyone.views.LoginView
import com.example.onlyone.views.MainView
import com.example.onlyone.views.SetUsernameView
import com.example.onlyone.views.SettingsView
import com.example.onlyone.views.ShopView
import com.example.onlyone.views.SplashView
import kotlinx.coroutines.delay

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentUser by sessionViewModel.currentUser.collectAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val bannerMessage = remember { mutableStateOf("") }
    LaunchedEffect(true) {
        MessageNotifier.newMessageFlow.collect { (title, body) ->
            bannerMessage.value = "$title: $body"
            delay(3000)
            bannerMessage.value = ""
        }
    }

    if (currentRoute == Screen.MainScreen.route && currentUser != null) {
        RequestNotificationPermission()
    }


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
        TopSnackbar(message = bannerMessage.value)

        Scaffold(
            bottomBar = {
                if (currentRoute !in listOf(Screen.SplashScreen.route, Screen.LoginScreen.route)) {
                    MainBottomBar(navController = navController, currentRoute = currentRoute)
                }
            },
            // other scaffold content...
        ){ innerPadding ->
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
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }

                    composable(Screen.MainScreen.route) {
                        MainView(
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel,
                            navController = navController,
                            sessionViewModel = sessionViewModel
                        )
                    }

                    composable(Screen.FriendsScreen.route) {
                        FriendsView(userViewModel = userViewModel, navController = navController, chatViewModel = chatViewModel)
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

                    composable(Screen.SetUsernameScreen.route) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        SetUsernameView(uid = uid, email = email, userViewModel = userViewModel, navController = navController)
                    }

                    composable(
                        route = "ChatScreen/{uid}?isRandom={isRandom}",
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("isRandom") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: "none"
                        val isRandom = backStackEntry.arguments?.getBoolean("isRandom") ?: false

                        ChatScreenEntry(
                            uid = uid,
                            isRandom = isRandom,
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel
                        )
                    }

                }
            }
        }


    }
}


