package com.example.onlyone.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import kotlinx.coroutines.delay

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

    // Navigate after delay
    LaunchedEffect(firebaseUser) {
        delay(2000)
        if (firebaseUser == null) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val uid = firebaseUser!!.uid
            userViewModel.loadUser()
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Splash screen UI
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
