package com.example.onlyone

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.onlyone.composables.TopSnackbar
import com.example.onlyone.connection.ConnectivityListener
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
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
    val listener = remember { ConnectivityListener(context) }
    val isConnected by listener.isConnected.collectAsState(initial = null)
    val lastConnectionState = remember { mutableStateOf(true) } // store previous state
    val showToast = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        listener.startListening()
    }

    DisposableEffect(Unit) {
        onDispose {
            listener.stopListening()
        }
    }

    LaunchedEffect(true) {
        MessageNotifier.newMessageFlow.collect { (title, body) ->
            bannerMessage.value = "$title: $body"
            delay(3000)
            bannerMessage.value = ""
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected == false && lastConnectionState.value) {
            showToast.value = true
            lastConnectionState.value = false
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            delay(5000)
            showToast.value = false
        } else if (isConnected == true && !lastConnectionState.value) {
            lastConnectionState.value = true
            Toast.makeText(context, "Back to life..", Toast.LENGTH_SHORT).show()
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

                    composable(
                        route = Screen.SetUsernameScreen.route,
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("email") { type = NavType.StringType },
                            navArgument("google") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        val isGoogleUser = backStackEntry.arguments?.getBoolean("google") ?: false

                        SetUsernameView(
                            uid = uid,
                            email = email,
                            userViewModel = userViewModel,
                            navController = navController,
                            isGoogleUser = isGoogleUser // ✅ Pass this
                        )
                    }

                    composable(
                        route = "ChatScreen/{uid}/{isRandom}",
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("isRandom") { type = NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: "none"
                        val isRandom = backStackEntry.arguments?.getBoolean("isRandom") ?: false
                        ChatScreenEntry(
                            uid = uid,
                            isRandom = isRandom,
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}


