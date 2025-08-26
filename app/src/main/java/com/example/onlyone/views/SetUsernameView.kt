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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
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

    // Chat language — can be broader
    val chatLanguageCodes = listOf("en", "de", "fr", "es", "pt")
    var selectedChatLanguage by remember { mutableStateOf("en") }
    var chatLangExpanded by remember { mutableStateOf(false) }

    // App language — ONLY en/de for now
    val appLanguageCodes = listOf("en", "de")
    var selectedAppLanguage by remember { mutableStateOf("en") }
    var appLangExpanded by remember { mutableStateOf(false) }

    // Localized display names
    val langName = mapOf(
        "en" to stringResource(R.string.lang_english),
        "de" to stringResource(R.string.lang_german),
        "fr" to stringResource(R.string.lang_french),
        "es" to stringResource(R.string.lang_spanish),
        "pt" to stringResource(R.string.lang_portuguese)
    )

    // Safety: force app language to one of the supported codes
    fun clampAppLanguage(code: String): String =
        if (code in appLanguageCodes) code else "en"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.onboarding_title))
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.onboarding_username_label)) }
        )

        Spacer(Modifier.height(16.dp))

        // 💬 Chat Language
        Text(stringResource(R.string.onboarding_chat_language_label))
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clickable { chatLangExpanded = true }) {
            OutlinedTextField(
                value = langName[selectedChatLanguage] ?: selectedChatLanguage,
                onValueChange = {},
                label = { Text(stringResource(R.string.onboarding_chat_language_label)) },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = chatLangExpanded, onDismissRequest = { chatLangExpanded = false }) {
                chatLanguageCodes.forEach { code ->
                    DropdownMenuItem(onClick = {
                        selectedChatLanguage = code
                        chatLangExpanded = false
                    }) { Text(langName[code] ?: code.uppercase()) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🌐 App Language (EN/DE only)
        /*Text(stringResource(R.string.onboarding_app_language_label))
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clickable { appLangExpanded = true }) {
            OutlinedTextField(
                value = langName[selectedAppLanguage] ?: selectedAppLanguage,
                onValueChange = {},
                label = { Text(stringResource(R.string.onboarding_app_language_label)) },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = appLangExpanded, onDismissRequest = { appLangExpanded = false }) {
                appLanguageCodes.forEach { code ->
                    DropdownMenuItem(onClick = {
                        selectedAppLanguage = code
                        appLangExpanded = false
                    }) { Text(langName[code] ?: code.uppercase()) }
                }
            }
        }*/

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
                                val appLang = clampAppLanguage(selectedAppLanguage)

                                userViewModel.saveSearchUserLanguage(selectedChatLanguage)
                                userViewModel.saveAppLanguage(appLang)

                                applyAppLocale(appLang)

                                userViewModel.loadUser()
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                }
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.onboarding_error_create_profile),
                                    Toast.LENGTH_SHORT
                                ).show()
                                isSaving = false
                            }
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            context.getString(R.string.onboarding_error_fcm_token),
                            Toast.LENGTH_SHORT
                        ).show()
                        isSaving = false
                    }
            },
            enabled = !isSaving
        ) {
            Text(
                if (isSaving) stringResource(R.string.common_saving)
                else stringResource(R.string.onboarding_continue)
            )
        }
    }
}


