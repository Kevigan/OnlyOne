package com.example.onlyone.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SplashView(
    sessionViewModel: SessionViewModel,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val firebaseUser by sessionViewModel.currentUser.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val offsetX = remember { Animatable(-screenWidth.value) }

    // Animate logo from left to center
    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Navigation gate: auth -> verify -> profile -> main
    LaunchedEffect(firebaseUser) {
        // Let the animation breathe a bit
        delay(1200)

        val auth = Firebase.auth
        val user = auth.currentUser

        if (user == null) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // Reload to ensure email_verified claim is fresh
        user.reload().addOnCompleteListener {
            val isVerified = auth.currentUser?.isEmailVerified == true
            val uid = auth.currentUser?.uid.orEmpty()
            val email = auth.currentUser?.email.orEmpty()

            if (!isVerified) {
                // NOTE: If you haven't added Screen.VerifyEmailScreen yet,
                // use the raw route string below to avoid compile errors:
                // navController.navigate("verify_email/$uid/$email") { ... }
                navController.navigate(
                    // Replace with Screen.VerifyEmailScreen.createRoute(uid, email) once added
                    "verify_email/$uid/$email"
                ) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
                return@addOnCompleteListener
            }

            // Verified — check if profile exists in users_public
            Firebase.firestore.collection("users_public").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // Verified + profile exists → Main
                        userViewModel.loadUser()
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    } else {
                        // Verified but no profile → SetUsername flow
                        navController.navigate(
                            Screen.SetUsernameScreen.createRoute(uid, email, isGoogleUser = false)
                        ) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener {
                    // If unsure, err on the safe side: send to verify screen again
                    navController.navigate("verify_email/$uid/$email") {
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                    }
                }
        }
    }

    // UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_only_one),
                contentDescription = "OnlyOne Logo",
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Only One\nMake it count!",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}
