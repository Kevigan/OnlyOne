package com.example.onlyone.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.Screen
import com.example.onlyone.viewModels.SessionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun VerifyEmailView(
    uid: String,
    email: String,
    sessionViewModel: SessionViewModel,
    navController: NavController
) {
    // block back button from leaving the gate
    androidx.activity.compose.BackHandler(true) { /* no-op */ }

    var busy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verify your email"); Spacer(Modifier.height(8.dp))
        Text(email); Spacer(Modifier.height(16.dp))
        info?.let { Text(it) }; Spacer(Modifier.height(16.dp))

        Button(enabled = !busy, onClick = {
            busy = true
            sessionViewModel.sendEmailVerification(
                onSuccess = { info = "Verification email sent."; busy = false },
                onFailure = { e -> info = e ?: "Failed to send email."; busy = false }
            )
        }) { Text("Resend email") }

        Spacer(Modifier.height(8.dp))

        Button(enabled = !busy, onClick = {
            busy = true
            sessionViewModel.reloadAndIsEmailVerified(
                onResult = { verified ->
                    busy = false
                    if (verified) {
                        navController.navigate(
                            Screen.SetUsernameScreen.createRoute(uid, email)
                        ) {
                            popUpTo(Screen.VerifyEmailScreen.route) { inclusive = true }
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    } else {
                        info = "Not verified yet. Please tap the link in your email."
                    }
                },
                onFailure = { e -> busy = false; info = e ?: "Could not check verification." }
            )
        }) { Text("I’ve verified") }
    }
}
