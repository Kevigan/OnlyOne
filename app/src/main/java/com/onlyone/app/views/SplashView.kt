package com.onlyone.app.views

import android.app.Activity
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.Screen
import com.onlyone.app.ads.LocalConsentManager
import com.onlyone.app.viewModels.SessionViewModel
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onlyone.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SplashView(
    sessionViewModel: SessionViewModel,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val consentManager = LocalConsentManager.current
    val context = LocalContext.current
    val activity = context as Activity

    val firebaseUser by sessionViewModel.currentUser.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val offsetX = remember { Animatable(-screenWidth.value) }

    // ✅ composable-owned scope for launching coroutines from callbacks
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        consentManager.requestAndShowIfRequired(activity) { canRequestAds, err ->
            Log.d("UMP", "Finished consent flow. canRequestAds=$canRequestAds, err=$err")
        }
    }

    LaunchedEffect(firebaseUser) {
        delay(1200)

        val auth = Firebase.auth
        val user = auth.currentUser

        if (user == null) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        user.reload().addOnCompleteListener {
            val isVerified = auth.currentUser?.isEmailVerified == true
            val uid = auth.currentUser?.uid.orEmpty()
            val email = auth.currentUser?.email.orEmpty()

            if (!isVerified) {
                navController.navigate("verify_email/$uid/$email") {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
                return@addOnCompleteListener
            }

            Firebase.firestore.collection("users_public").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // Cheap load every start
                        userViewModel.loadUser(checkChanged = false)

                        // ✅ use the composable scope, NOT LaunchedEffect here
                        scope.launch {
                            // optional tiny delay to stagger network work
                            // delay(8000)
                            //userViewModel.refreshFriendDeltasIfDue(hours = 6, subsetSize = 12)
                            userViewModel.refreshFriendDeltasIfDue(hours = 0, subsetSize = Int.MAX_VALUE)
                        }

                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(
                            Screen.AgeGateScreen.createRoute(uid, email, google = false)
                        ) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener {
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
                painter = painterResource(id = R.drawable.new_logo),
                contentDescription = "OnlyOne Logo",
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .size(320.dp)
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
