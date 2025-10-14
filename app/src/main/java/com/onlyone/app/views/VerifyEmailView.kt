package com.onlyone.app.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.Screen
import com.onlyone.app.R
import com.onlyone.app.viewModels.SessionViewModel

@Composable
fun VerifyEmailView(
    uid: String,
    email: String,
    sessionViewModel: SessionViewModel,
    navController: NavController
) {
    BackHandler(true) { /* block back navigation */ }

    val context = LocalContext.current

    var busy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.verify_email_title))
        Spacer(Modifier.height(8.dp))
        Text(email)
        Spacer(Modifier.height(16.dp))
        info?.let { Text(it) }
        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !busy,
            onClick = {
                busy = true
                sessionViewModel.sendEmailVerification(
                    onSuccess = {
                        info = context.getString(R.string.verify_email_sent)
                        busy = false
                    },
                    onFailure = { e ->
                        info = e ?: context.getString(R.string.verify_email_failed)
                        busy = false
                    }
                )
            }
        ) {
            Text(stringResource(R.string.verify_email_resend))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            enabled = !busy,
            onClick = {
                busy = true
                sessionViewModel.reloadAndIsEmailVerified(
                    onResult = { verified ->
                        busy = false
                        if (verified) {
                            navController.navigate(
                                Screen.AgeGateScreen.createRoute(uid, email, false)
                            ) {
                                popUpTo(Screen.VerifyEmailScreen.route) { inclusive = true }
                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                            }
                        } else {
                            info = context.getString(R.string.verify_email_not_verified)
                        }
                    },
                    onFailure = { e ->
                        busy = false
                        info = e ?: context.getString(R.string.verify_email_check_failed)
                    }
                )
            }
        ) {
            Text(stringResource(R.string.verify_email_verified))
        }
    }
}
