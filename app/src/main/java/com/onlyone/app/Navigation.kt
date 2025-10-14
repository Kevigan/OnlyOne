package com.onlyone.app

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.firebase.firestore.FirebaseFirestore // ✅ NEW
import com.onlyone.app.cloudMessaging.FriendRequestNotifier
import com.onlyone.app.cloudMessaging.MessageNotifier
import com.onlyone.app.cloudMessaging.RequestNotificationPermission
import com.onlyone.app.composables.ChatScreenEntry
import com.onlyone.app.composables.MainBottomBar
import com.onlyone.app.composables.TopSnackbar
import com.onlyone.app.connection.ConnectivityListener
import com.onlyone.app.repositories.UserChainRepo // ✅ NEW
import com.onlyone.app.theme.ThemeViewModel
import com.onlyone.app.ui.TransparentSystemBars
import com.onlyone.app.utils.toPublicUser
import com.onlyone.app.viewModels.ChatViewModel
import com.onlyone.app.viewModels.SessionViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.onlyone.app.views.AgeGateView
import com.onlyone.app.views.FriendsView
import com.onlyone.app.views.LoginView
import com.onlyone.app.views.MainView
import com.onlyone.app.views.SetUsernameView
import com.onlyone.app.views.SplashView
import com.onlyone.app.views.VerifyEmailView
import com.onlyone.app.views.achievements.AchievementsView
import com.onlyone.app.views.chain.CreateChainMessageView
import com.onlyone.app.views.chainMessage.ChainDetailScreen
import com.onlyone.app.views.chainMessage.SavedChainsScreen
import com.onlyone.app.views.chat.ChatViewFriendsOnly
import com.onlyone.app.views.feedback.UserFeedbackView
import com.onlyone.app.views.settingsView.SettingsView
import com.onlyone.app.views.shopView.ShopView
import com.onlyone.app.views.chainMessage.ChainsView
import com.onlyone.ui.onboarding.OnboardingView
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
    val lastConnectionState = remember { mutableStateOf(true) }
    val showToast = remember { mutableStateOf(false) }
    val tokens by themeViewModel.tokens.collectAsState()
    val theme = themeViewModel.tokens.collectAsState().value

    // ✅ include ChainsScreen route so bottom bar shows there too
    val bottomBarRoutes = setOf(
        Screen.MainScreen.route,
        Screen.FriendsScreen.route,
        Screen.ShopScreen.route,
        Screen.SettingsScreen.route,
        Screen.AchievementsScreen.route,
        Screen.CreateChainMessageScreen.route,
        Screen.ChainsScreen.route,
        Screen.ChainDetailScreen.route,
        Screen.SavedChainsScreen.route,
        "ChatScreen",
        "ChatFriends"
    )
    TransparentSystemBars(darkIcons = tokens.systemBarDarkIcons)

    val incomingRequests by userViewModel.incomingRequestUsernames.collectAsState()
    val hasIncomingRequests = incomingRequests.isNotEmpty()

    fun String.baseRoute(): String = this.substringBefore("/")

    val showBottomBar = navBackStackEntry
        ?.destination
        ?.hierarchy
        ?.any { dest ->
            val destRoute = dest.route?.baseRoute()
            bottomBarRoutes.any { it.baseRoute() == destRoute }
        } == true

    LaunchedEffect(tokens) {
        Log.d("ThemeToken", "Navigation tokens changed -> bgRes=${tokens.backgroundRes}")
    }

    LaunchedEffect(Unit) { listener.startListening() }
    DisposableEffect(Unit) { onDispose { listener.stopListening() } }

    LaunchedEffect(true) {
        MessageNotifier.newMessageFlow.collect { (title, body) ->
            bannerMessage.value = "$title: $body"
            delay(3000)
            bannerMessage.value = ""
        }
    }

    LaunchedEffect(true) {
        FriendRequestNotifier.newFriendRequestFlow.collect { event ->
            bannerMessage.value = event.message ?: ""
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
        tokens.backgroundRes?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
            )
        } ?: run {
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
                    MainBottomBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        theme = theme,
                        showFriendsPlus = hasIncomingRequests
                    )
                }
            }
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
                        ShopView(userViewModel = userViewModel, themeViewModel = themeViewModel, theme = theme)
                    }

                    composable(Screen.SettingsScreen.route) {
                        SettingsView(userViewModel = userViewModel, sessionViewModel = sessionViewModel, navController = navController, theme = theme)
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

                    composable(
                        route = Screen.ChatFriendsScreen.route,
                        arguments = listOf(navArgument("uid"){ type = NavType.StringType })
                    ) { backStackEntry ->
                        val friendUid = backStackEntry.arguments?.getString("uid").orEmpty()
                        val friendFromTarget by userViewModel.targetUser.collectAsState(initial = null)
                        val localFriends by userViewModel.observeLocalFriends().collectAsState(initial = emptyList())
                        val localFriend = remember(localFriends, friendUid) {
                            localFriends.firstOrNull { it.uid == friendUid }
                        }
                        val me by userViewModel.user.observeAsState()

                        val friendPublic = friendFromTarget ?: localFriend?.toPublicUser()

                        if (me != null && friendPublic != null) {
                            ChatViewFriendsOnly(
                                user = me!!,
                                friend = friendPublic,
                                chatViewModel = chatViewModel,
                                userViewModel = userViewModel,
                                navController = navController,
                                theme = theme,
                                ageOverride = localFriend?.age,
                                genderOverride = localFriend?.gender,
                                cityOverride = localFriend?.city,
                                achievementCountOverride = localFriend?.achievementCount
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    composable(Screen.AchievementsScreen.route) {
                        AchievementsView(viewModel = userViewModel, theme = theme)
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
                        BackHandler(enabled = true) { /* block back */ }
                        OnboardingView(
                            theme = theme,
                            onDismiss = {
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(0); launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(
                        route = Screen.AgeGateScreen.route,
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("email") { type = NavType.StringType },
                            navArgument("google") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid").orEmpty()
                        val email = backStackEntry.arguments?.getString("email").orEmpty()
                        val isGoogleUser = backStackEntry.arguments?.getBoolean("google") ?: false

                        AgeGateView(
                            uid = uid,
                            email = email,
                            isGoogleUser = isGoogleUser,
                            theme = theme,
                            navController = navController
                        )
                    }

                    composable(Screen.CreateChainMessageScreen.route) {
                        CreateChainMessageView(
                            theme = theme,
                            userViewModel = userViewModel,
                            navController = navController
                        )
                    }

                    // ✅ NEW: list all open chains for this user (participants) and those assigned to them
                    composable(Screen.ChainsScreen.route) {
                        ChainsView(
                            theme = theme,
                            userViewModel = userViewModel,
                            navController = navController
                        )
                    }

                    composable(Screen.ChainDetailScreen.route) { backStackEntry ->
                        val chainId = backStackEntry.arguments?.getString("chainId") ?: return@composable
                        ChainDetailScreen(
                            theme = theme,
                            userViewModel = userViewModel,
                            navController = navController,
                            chainId = chainId
                        )
                    }

                    composable(Screen.SavedChainsScreen.route) {
                        SavedChainsScreen(
                            theme = theme,
                            userViewModel = userViewModel,
                            navController = navController
                        )
                    }

                }
            }
        }
    }
}

