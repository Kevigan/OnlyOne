package com.example.onlyone

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
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
import com.example.onlyone.theme.ThemeViewModel
import com.example.onlyone.ui.TransparentSystemBars
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.example.onlyone.views.FriendsView
import com.example.onlyone.views.LoginView
import com.example.onlyone.views.MainView
import com.example.onlyone.views.SetUsernameView
import com.example.onlyone.views.settingsView.SettingsView
import com.example.onlyone.views.shopView.ShopView
import com.example.onlyone.views.SplashView
import com.example.onlyone.views.VerifyEmailView
import com.example.onlyone.views.achievements.AchievementsView
import com.example.onlyone.views.feedback.UserFeedbackView
import com.yourapp.ui.onboarding.OnboardingView
import kotlinx.coroutines.delay


@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
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
    val tokens by themeViewModel.tokens.collectAsState()
    val theme = themeViewModel.tokens.collectAsState().value
    val bottomBarRoutes = setOf(
        Screen.MainScreen.route,
        Screen.FriendsScreen.route,
        Screen.ShopScreen.route,
        Screen.SettingsScreen.route,
        Screen.AchievementsScreen.route,
        "ChatScreen"
    )
    TransparentSystemBars(darkIcons = tokens.systemBarDarkIcons)

    // 2) Helper to strip arguments
    fun String.baseRoute(): String = this.substringBefore("/")

// 3) Use base-route comparison against the hierarchy
    val showBottomBar = navBackStackEntry
        ?.destination
        ?.hierarchy
        ?.any { dest ->
            val destRoute = dest.route?.baseRoute()
            bottomBarRoutes.any { it.baseRoute() == destRoute }
        } == true

    LaunchedEffect(tokens) {
        // Quick sanity check in Logcat whenever theme changes
        Log.d("ThemeToken", "Navigation tokens changed -> bgRes=${tokens.backgroundRes}")
    }

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
            .padding(top = 0.dp)

    ) {
        // ---- THEME BACKGROUND (first child, behind everything) ----
        tokens.backgroundRes?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Optional contrast veil
            Box(
                modifier = Modifier
                    .matchParentSize()
                        //.background(tokens.overlayColor)
            )
        } ?: run {
            // Fallback if a theme has no image
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colors.background)
            )
        }
        TopSnackbar(message = bannerMessage.value)

        Scaffold(
            backgroundColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    MainBottomBar(navController = navController, currentRoute = currentRoute, theme = theme)
                }
            }
            // other scaffold content...
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
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }

                    composable(Screen.MainScreen.route) {
                        MainView(
                            userViewModel = userViewModel,
                            chatViewModel = chatViewModel,
                            navController = navController,
                            sessionViewModel = sessionViewModel,
                            theme = theme
                        )
                    }

                    composable(Screen.FriendsScreen.route) {
                        FriendsView(
                            userViewModel = userViewModel,
                            navController = navController,
                            chatViewModel = chatViewModel,
                            theme = theme,
                        )
                    }

                    composable(Screen.ShopScreen.route) {
                        ShopView(userViewModel = userViewModel, themeViewModel = themeViewModel,theme = theme)
                    }

                    composable(Screen.SettingsScreen.route) {
                        SettingsView(userViewModel = userViewModel,sessionViewModel = sessionViewModel,navController = navController, theme = theme)
                    }

                    composable(Screen.LoginScreen.route) {
                        LoginView(
                            theme = theme,
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
                            isGoogleUser = isGoogleUser,
                            theme = theme
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
                            navController = navController,
                            theme = theme
                        )
                    }

                    composable(Screen.AchievementsScreen.route) {
                        AchievementsView(viewModel = userViewModel)
                    }

                    composable(
                        route = Screen.VerifyEmailScreen.route,
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("email") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        VerifyEmailView(
                            uid = uid,
                            email = email,
                            sessionViewModel = sessionViewModel,
                            navController = navController
                        )
                    }

                    composable(Screen.FeedbackScreen.route) {
                        UserFeedbackView(
                            navController = navController,
                            userViewModel = userViewModel,
                            theme = theme
                        )
                    }

                    composable(Screen.OnboardingScreen.route) {

                        // Block system back entirely while on onboarding
                        BackHandler(enabled = true) {
                            // do nothing -> stays on OnboardingView
                        }

                        OnboardingView(
                            theme = theme,
                            onDismiss = {
                                // Only the Finish button moves on to Main
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(0)               // clean stack so onboarding isn’t revisitable
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


