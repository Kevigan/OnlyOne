package com.example.onlyone.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.viewModels.SessionViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashView(
    sessionViewModel: SessionViewModel,
    navController: NavController
) {
    val firebaseUser by sessionViewModel.currentUser.collectAsState()

    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val offsetX = remember { androidx.compose.animation.core.Animatable(-screenWidthPx) } // Float in px

    // Animate the logo in from the left
    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Navigate after delay
    LaunchedEffect(firebaseUser) {
        delay(2000) // Let animation finish
        if (firebaseUser == null) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.MainScreen.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Splash screen content
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val offsetInPx = offsetX.value

        Image(
            painter = painterResource(id = R.drawable.logo_only_one),
            contentDescription = "OnlyOne Logo",
            modifier = Modifier
                .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                .size(160.dp)
        )
    }
}
