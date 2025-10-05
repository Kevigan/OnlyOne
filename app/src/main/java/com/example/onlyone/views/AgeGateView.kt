package com.example.onlyone.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.Screen
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.R

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
        Text(
            text = stringResource(R.string.agegate_title),
            style = MaterialTheme.typography.h6,
            color = theme.textColor
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmed, onCheckedChange = { confirmed = it })
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.agegate_checkbox_label),
                color = theme.textColor
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = confirmed,
            onClick = {
                // We only navigate when confirmed = true.
                navController.navigate(
                    Screen.SetUsernameScreen.createRoute(uid, email, isGoogleUser)
                ) {
                    popUpTo(Screen.AgeGateScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_continue))
        }
    }
}
