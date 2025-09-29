package com.example.onlyone.views

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.onlyone.views.chat.ChatLanguageHelpDialog
import com.google.firebase.messaging.FirebaseMessaging

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

    // Chat language — broader Europe set
    val chatLanguageCodes = listOf(
        "en","de","fr","es","pt","it","nl","pl","ru","tr","uk","cs","ro","hu","sv"
    )
    var selectedChatLanguage by remember { mutableStateOf("en") }
    var chatLangExpanded by remember { mutableStateOf(false) }

    // App language — ONLY en/de
    val appLanguageCodes = listOf("en", "de")
    var selectedAppLanguage by remember { mutableStateOf("en") }

    // Localized display names
    val langName = mapOf(
        "en" to stringResource(R.string.lang_english),
        "de" to stringResource(R.string.lang_german),
        "fr" to stringResource(R.string.lang_french),
        "es" to stringResource(R.string.lang_spanish),
        "pt" to stringResource(R.string.lang_portuguese),
        "it" to stringResource(R.string.lang_italian),
        "nl" to stringResource(R.string.lang_dutch),
        "pl" to stringResource(R.string.lang_polish),
        "ru" to stringResource(R.string.lang_russian),
        "tr" to stringResource(R.string.lang_turkish),
        "uk" to stringResource(R.string.lang_ukrainian),
        "cs" to stringResource(R.string.lang_czech),
        "ro" to stringResource(R.string.lang_romanian),
        "hu" to stringResource(R.string.lang_hungarian),
        "sv" to stringResource(R.string.lang_swedish),
    )

    var showChatLangInfo by remember { mutableStateOf(false) }

    fun clampAppLanguage(code: String): String =
        if (code in appLanguageCodes) code else "en"

    // Helpers
    fun sanitizeAgeInput(input: String): String {
        val digits = input.filter { it.isDigit() }.take(3)
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
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }

        OutlinedTextField(
            value = ageText,
            onValueChange = { new -> ageText = if (new.isBlank()) "" else sanitizeAgeInput(new) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("18–100", color = theme.textColor.copy(alpha = 0.6f)) },
            isError = ageError,
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
            onValueChange = { input -> city = input.take(50) },
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

        // Chat Language (label + help icon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_chat_language_label),
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { showChatLangInfo = true }) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = stringResource(R.string.settings_chat_language_help_title),
                    tint = Color.Yellow
                )
            }
        }

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
                                    popUpTo(0) { inclusive = true }
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
            enabled = !isSaving && ageValid,
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

    // Reuse the same dialog style used in settings (wraps CustomAlertDialog)
    if (showChatLangInfo) {
        ChatLanguageHelpDialog(
            theme = theme,
            onDismiss = { showChatLangInfo = false }
        )
    }
}
