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
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SetUsernameView(
    uid: String,
    email: String,
    userViewModel: UserViewModel,
    navController: NavController,
    isGoogleUser: Boolean = false
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // 🔤 Chat language (existing)
    val chatLanguageOptions = listOf("en", "de", "fr", "es", "it")
    var selectedChatLanguage by remember { mutableStateOf("en") }
    var chatLangExpanded by remember { mutableStateOf(false) }

    // 🛠 App language (new)
    // you can reuse the same options, or add "pt" if you support it
    val appLanguageOptions = listOf("en", "de", "fr", "es", "it")
    var selectedAppLanguage by remember { mutableStateOf("en") }
    var appLangExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose your username")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(Modifier.height(16.dp))

        // 💬 Chat Language
        Text("Select your chat language")
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clickable { chatLangExpanded = true }) {
            OutlinedTextField(
                value = selectedChatLanguage,
                onValueChange = {},
                label = { Text("Chat Language") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = chatLangExpanded, onDismissRequest = { chatLangExpanded = false }) {
                chatLanguageOptions.forEach { lang ->
                    DropdownMenuItem(onClick = {
                        selectedChatLanguage = lang
                        chatLangExpanded = false
                    }) { Text(lang) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🌐 App Language (NEW)
        Text("Select your app language")
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clickable { appLangExpanded = true }) {
            OutlinedTextField(
                value = selectedAppLanguage,
                onValueChange = {},
                label = { Text("App Language") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = appLangExpanded, onDismissRequest = { appLangExpanded = false }) {
                appLanguageOptions.forEach { lang ->
                    DropdownMenuItem(onClick = {
                        selectedAppLanguage = lang
                        appLangExpanded = false
                    }) { Text(lang) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                isSaving = true
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        userViewModel.repository.createUserProfile(
                            email = email,
                            username = username,
                            fcmToken = token,
                            chatLanguage = selectedChatLanguage,
                            onSuccess = {
                                // persist both prefs
                                userViewModel.saveSearchUserLanguage(selectedChatLanguage)
                                userViewModel.saveAppLanguage(selectedAppLanguage)

                                // apply locale immediately
                                applyAppLocale(selectedAppLanguage)

                                userViewModel.loadUser()
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                }
                            },
                            onFailure = {
                                Toast.makeText(context, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                                isSaving = false
                            }
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                        isSaving = false
                    }
            },
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Continue")
        }
    }
}


