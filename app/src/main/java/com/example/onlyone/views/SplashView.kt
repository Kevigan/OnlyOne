package com.example.onlyone.views

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
import com.example.onlyone.BuildConfig
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.ads.LocalConsentManager
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
    val consentManager = LocalConsentManager.current           // ✅ get manager
    val context = LocalContext.current
    val activity = context as Activity

    // existing state/animation code...
    val firebaseUser by sessionViewModel.currentUser.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val offsetX = remember { Animatable(-screenWidth.value) }

    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // ✅ Ask for consent on cold start. Safe to call each launch.
    LaunchedEffect(Unit) {
        //if (BuildConfig.DEBUG) consentManager.resetForTesting()

        consentManager.requestAndShowIfRequired(activity) { canRequestAds, err ->
            Log.d("UMP", "Finished consent flow. canRequestAds=$canRequestAds, err=$err")
            // If you initialize ads, do it here when canRequestAds==true.
        }
    }

    // 🧭 Your existing navigation gate can remain as-is
    LaunchedEffect(firebaseUser) {
        delay(1200) // let the logo animate

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
                        userViewModel.loadUser()
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(
                            Screen.AgeGateScreen.createRoute(uid, email, google = false)
                        ) { popUpTo(Screen.SplashScreen.route) { inclusive = true } }
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
