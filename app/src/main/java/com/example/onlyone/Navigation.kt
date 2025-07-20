package com.example.onlyone

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
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
                        val currentUser by userViewModel.user.observeAsState()
                        val targetUser by chatViewModel.targetUser.collectAsState()

                        // 💡 Load data once user is known
                        LaunchedEffect(isRandom, uid, currentUser?.uid) {
                            if (currentUser == null) return@LaunchedEffect

                            val currentUid = currentUser!!.uid
                            Log.d("ChatScreen", "LaunchedEffect → isRandom=$isRandom, uid=$uid")

                            if (isRandom) {
                                if (chatViewModel.userQueue.value.isEmpty()) {
                                    Log.d("ChatScreen", "→ Loading random user batch")
                                    chatViewModel.loadRandomUserBatch(
                                        currentUserId = currentUid,
                                        userRepository = userViewModel.repository,
                                        onNotEnoughSwipes = {
                                            Log.w("ChatScreen", "⚠️ Not enough swipes")
                                            // Optionally: navigate back or show dialog
                                        },
                                        onComplete = { success ->
                                            Log.d("ChatScreen", "✅ Batch loaded: success=$success")
                                        }
                                    )
                                }
                            } else {
                                Log.d("ChatScreen", "→ Loading target user uid=$uid")
                                chatViewModel.loadTargetUser(uid, isRandom = false, userViewModel)
                            }
                        }
                        Log.d("ChatScreen", "⏳ Waiting: currentUser=${currentUser?.uid}, targetUser=${targetUser?.uid}")

                        // 👁️ Show ChatView if both users are ready
                        if (currentUser != null && targetUser != null) {
                            ChatView(
                                user = currentUser!!,
                                targetUser = targetUser!!,
                                isRandom = isRandom,
                                onNextUser = { chatViewModel.consumeNextUserFromQueue() },
                                chatViewModel = chatViewModel,
                                userRepository = userViewModel.userRepository
                            )
                        } else if (currentUser != null && isRandom && chatViewModel.userQueue.value.isEmpty()) {
                            // 🛑 No more users in queue
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎉 You've seen everyone for now!", style = MaterialTheme.typography.h2)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { navController.popBackStack() }) {
                                        Text("Back to Home")
                                    }
                                }
                            }
                        } else {
                            // ⏳ Still loading
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


