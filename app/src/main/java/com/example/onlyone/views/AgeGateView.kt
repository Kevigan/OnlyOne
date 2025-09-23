package com.example.onlyone.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.Screen
import com.example.onlyone.theme.ThemeTokens

@Composable
fun AgeGateView(
    uid: String,
    email: String,
    isGoogleUser: Boolean,
    theme: ThemeTokens,
    navController: NavController
) {
    var confirmed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Only One is 18+.", style = MaterialTheme.typography.h6, color = theme.textColor)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Spacer(Modifier.width(8.dp))
            Text("I confirm I am 18 or older", color = theme.textColor)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = confirmed,
            onClick = {
                // We only ever reach SetUsername if confirmed=true,
                // so it can always send ageAffirmation = true to backend.
                navController.navigate(
                    Screen.SetUsernameScreen.createRoute(uid, email, isGoogleUser)
                ) {
                    popUpTo(Screen.AgeGateScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}
