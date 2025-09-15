package com.example.onlyone.views

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.max
import kotlin.math.min

@Composable
fun SetUsernameView(
    uid: String,
    email: String,
    userViewModel: UserViewModel,
    navController: NavController,
    isGoogleUser: Boolean = false,
    theme: ThemeTokens, // ⬅️ added so we can style like AvatarsSection
) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // NEW FIELDS
    var ageText by remember { mutableStateOf("") }     // keep as text for easier input control
    var gender by remember { mutableStateOf("m") }     // allowed: m, f, d
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("m", "f", "d")
    var city by remember { mutableStateOf("") }

    // Chat language — can be broader
    val chatLanguageCodes = listOf("en", "de", "fr", "es", "pt")
    var selectedChatLanguage by remember { mutableStateOf("en") }
    var chatLangExpanded by remember { mutableStateOf(false) }

    // App language — ONLY en/de for now
    val appLanguageCodes = listOf("en", "de")
    var selectedAppLanguage by remember { mutableStateOf("en") }

    // Localized display names
    val langName = mapOf(
        "en" to stringResource(R.string.lang_english),
        "de" to stringResource(R.string.lang_german),
        "fr" to stringResource(R.string.lang_french),
        "es" to stringResource(R.string.lang_spanish),
        "pt" to stringResource(R.string.lang_portuguese)
    )

    fun clampAppLanguage(code: String): String =
        if (code in appLanguageCodes) code else "en"

    // Helpers
    fun clampAgeInput(input: String): String {
        // digits only
        val digits = input.filter { it.isDigit() }.take(2) // 2 digits is enough for 1..99
        if (digits.isEmpty()) return ""
        val num = digits.toInt()
        val clamped = min(99, max(1, num))
        return clamped.toString()
    }

    fun isFormValid(): Boolean {
        val ageOk = ageText.toIntOrNull()?.let { it in 1..99 } == true
        val genderOk = gender in genderOptions
        val cityOk = city.isNotBlank() && city.length <= 50
        val usernameOk = username.isNotBlank()
        return ageOk && genderOk && cityOk && usernameOk
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Username
        Text(
            text = stringResource(R.string.onboarding_username_label),
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Age
        Text(
            text = stringResource(R.string.onboarding_age_label), // add in strings.xml
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = ageText,
            onValueChange = { new ->
                // live clamp to 1..99 while typing
                ageText = when {
                    new.isBlank() -> ""
                    else -> clampAgeInput(new)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("1–99") }
        )

        Spacer(Modifier.height(16.dp))

        // Gender
        Text(
            text = stringResource(R.string.onboarding_gender_label), // add in strings.xml
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .clickable { genderExpanded = true }
        ) {
            OutlinedTextField(
                value = gender.uppercase(),
                onValueChange = { /* read-only */ },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("M / F / D") }
            )
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach { code ->
                    DropdownMenuItem(onClick = {
                        gender = code
                        genderExpanded = false
                    }) {
                        Text(code.uppercase())
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // City
        Text(
            text = stringResource(R.string.onboarding_city_label), // add in strings.xml
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = city,
            onValueChange = { input ->
                city = input.take(50) // hard cap to 50 chars
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.onboarding_city_placeholder)) } // add in strings.xml
        )

        Spacer(Modifier.height(16.dp))

        // Chat Language
        Text(
            text = stringResource(R.string.onboarding_chat_language_label),
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp)
        )
        Box(Modifier
            .fillMaxWidth()
            .clickable { chatLangExpanded = true }
        ) {
            OutlinedTextField(
                value = langName[selectedChatLanguage] ?: selectedChatLanguage,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = chatLangExpanded,
                onDismissRequest = { chatLangExpanded = false }
            ) {
                chatLanguageCodes.forEach { code ->
                    DropdownMenuItem(onClick = {
                        selectedChatLanguage = code
                        chatLangExpanded = false
                    }) { Text(langName[code] ?: code.uppercase()) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isFormValid()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.onboarding_form_invalid), // add in strings.xml
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                isSaving = true
                val age = ageText.toInt() // safe due to validation

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        userViewModel.repository.createUserProfile(
                            email = email,
                            username = username,
                            fcmToken = token,
                            chatLanguage = selectedChatLanguage,
                            // ⬇️ NEW FIELDS (ensure your repo signature supports these)
                            age = age,
                            gender = gender, // "m" | "f" | "d"
                            city = city,
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
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSaving)
                    stringResource(R.string.common_saving)
                else
                    stringResource(R.string.onboarding_continue)
            )
        }
    }
}
