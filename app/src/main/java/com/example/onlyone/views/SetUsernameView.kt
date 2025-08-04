package com.example.onlyone.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
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
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SetUsernameView(
    uid: String,
    email: String,
    userViewModel: UserViewModel,
    navController: NavController,
    isGoogleUser: Boolean = false // 👈 new param
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 🔤 Language dropdown state
    val languageOptions = listOf("en", "de", "fr", "es", "it")
    var selectedLanguage by remember { mutableStateOf("en") }
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose your username")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select your chat language")
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { languageDropdownExpanded = true }) {

            OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                label = { Text("Chat Language") },
                readOnly = true,
                enabled = false, // disables internal tap logic
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = languageDropdownExpanded,
                onDismissRequest = { languageDropdownExpanded = false }
            ) {
                languageOptions.forEach { lang ->
                    DropdownMenuItem(onClick = {
                        selectedLanguage = lang
                        languageDropdownExpanded = false
                    }) {
                        Text(lang)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isSaving = true

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        userViewModel.repository.createUserProfile(
                            email = email,
                            username = username,
                            fcmToken = token,
                            chatLanguage = selectedLanguage,
                            onSuccess = {
                                userViewModel.saveSearchUserLanguage(selectedLanguage)
                                userViewModel.loadUser()
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                }
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                                Log.e("SetUsername", "❌ Profile creation failed", error)
                                isSaving = false
                            }
                        )
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(context, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                        Log.e("SetUsername", "❌ FCM token fetch failed", error)
                        isSaving = false
                    }
            },
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Continue")
        }

    }
}


