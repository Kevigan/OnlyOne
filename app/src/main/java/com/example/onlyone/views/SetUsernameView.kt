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
    theme: ThemeTokens, // style consistently with the app
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
    // 1) replace your clampAgeInput with a non-clamping sanitizer
    fun sanitizeAgeInput(input: String): String {
        val digits = input.filter { it.isDigit() }.take(3)
        // optionally cap max to 100 only when 3 digits
        return if (digits.length == 3) {
            val n = digits.toInt()
            if (n > 100) "100" else digits
        } else digits
    }

    fun isFormValid(): Boolean {
        val ageOk = ageText.toIntOrNull()?.let { it in 18..100 } == true
        val genderOk = gender in genderOptions
        val cityOk = city.isNotBlank() && city.length <= 50
        val usernameOk = username.isNotBlank()
        return ageOk && genderOk && cityOk && usernameOk
    }

    // DRY: themed colors for all text fields
    @Composable
    fun themedTextFieldColors(theme: ThemeTokens) =
        TextFieldDefaults.outlinedTextFieldColors(
            textColor = theme.textColor,
            cursorColor = theme.textColor,
            focusedBorderColor = theme.textColor,
            unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
            disabledTextColor = theme.textColor
        )

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
            modifier = Modifier.fillMaxWidth(),
            colors = themedTextFieldColors(theme)
        )

        Spacer(Modifier.height(16.dp))

        // ===== Age =====
        val ageInt = ageText.toIntOrNull()
        val ageError = ageText.isNotEmpty() && (ageInt == null || ageInt !in 18..100)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_age_label),
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "min. 18",
                style = MaterialTheme.typography.caption,       // small
                color = MaterialTheme.colors.error              // red
            )
        }

        OutlinedTextField(
            value = ageText,
            onValueChange = { new -> ageText = if (new.isBlank()) "" else sanitizeAgeInput(new) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("18–100", color = theme.textColor.copy(alpha = 0.6f)) },
            isError = ageError,                                 // 🔴 turns border red
            colors = themedTextFieldColors(theme)
        )

        if (ageError) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Please enter an age between 18 and 100.",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }


        Spacer(Modifier.height(16.dp))

        // Gender
        Text(
            text = stringResource(R.string.onboarding_gender_label),
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
                placeholder = {
                    Text(
                        "M / F / D",
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                },
                colors = themedTextFieldColors(theme)
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
            text = stringResource(R.string.onboarding_city_label),
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
            placeholder = {
                Text(
                    stringResource(R.string.onboarding_city_placeholder),
                    color = theme.textColor.copy(alpha = 0.6f)
                )
            },
            colors = themedTextFieldColors(theme)
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
        Box(
            Modifier
                .fillMaxWidth()
                .clickable { chatLangExpanded = true }
        ) {
            OutlinedTextField(
                value = langName[selectedChatLanguage] ?: selectedChatLanguage,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = themedTextFieldColors(theme)
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
        val ageValid = ageText.toIntOrNull()?.let { it in 18..100 } == true
        Button(
            onClick = {
                if (!isFormValid()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.onboarding_form_invalid),
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
                            ageAffirmation = true,
                            onSuccess = {
                                val appLang = clampAppLanguage(selectedAppLanguage)

                                userViewModel.saveSearchUserLanguage(selectedChatLanguage)
                                userViewModel.saveAppLanguage(appLang)
                                applyAppLocale(appLang)

                                userViewModel.loadUser()
                                navController.navigate(Screen.OnboardingScreen.route) {
                                    popUpTo(0) { inclusive = true } // prevent back to SetUsername
                                    launchSingleTop = true
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
            enabled = !isSaving&& ageValid,
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
