package com.example.onlyone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.onlyone.composables.MainBottomBar
import com.example.onlyone.data.PublicUser
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.example.onlyone.views.ChatView
import com.example.onlyone.views.FriendsView
import com.example.onlyone.views.LoginView
import com.example.onlyone.views.MainView
import com.example.onlyone.views.SetUsernameView
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
                if (currentRoute !in listOf(Screen.SplashScreen.route, Screen.LoginScreen.route)) {
                    MainBottomBar(navController = navController, currentRoute = currentRoute)
                }
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
                        route = "ChatScreen/{uid}/{isFriend}",
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                            navArgument("isFriend") { type = NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
                        val isFriend = backStackEntry.arguments?.getBoolean("isFriend") ?: false

                        val currentUser by userViewModel.user.observeAsState()

                        val targetUser by chatViewModel.targetUser.collectAsState()

                        // Load user only once (unless UID changes)
                        LaunchedEffect(uid, isFriend) {
                            if (chatViewModel.targetUser.value?.uid != uid) {
                                chatViewModel.loadTargetUser(uid, isFriend, userViewModel)
                            }
                        }

                        if (currentUser != null && targetUser != null) {
                            ChatView(
                                user = currentUser!!,
                                targetUser = targetUser!!,
                                isFriend = isFriend,
                                chatViewModel = chatViewModel,
                                onNextUser = {
                                    userViewModel.repository.getRandomUserExcluding(
                                        excludeUid = currentUser!!.uid,
                                        excludeList = listOf(targetUser!!.uid) + userViewModel.user.value?.friendList.orEmpty()
                                    ) { randomUser ->
                                        if (randomUser != null) {
                                            val nextRoute = Screen.ChatScreen.createRoute(randomUser.uid, false)
                                            backStackEntry.savedStateHandle.get<NavController>("navController")?.navigate(nextRoute)
                                        } else {
                                            // fallback or toast: no more users available
                                        }
                                    }
                                }
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}


