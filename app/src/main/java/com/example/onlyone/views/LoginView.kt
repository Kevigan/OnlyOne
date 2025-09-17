package com.example.onlyone.views

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.BuildConfig
import com.example.onlyone.R
import com.example.onlyone.Screen
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.example.onlyone.views.settingsView.LanguageDropdown
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

// Top-level: used to track which action is running
enum class AuthBusy { LOGIN, REGISTER, GOOGLE }

@Composable
fun LoginView(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    theme: ThemeTokens
) {
    val context = LocalContext.current

    // --- Google sign-in client ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Busy gate
    var busyAction by remember { mutableStateOf<AuthBusy?>(null) }
    val isBusy = busyAction != null

    // Handle Google result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sessionViewModel.handleGoogleSignInResult(
            resultData = result.data,
            onSuccess = { uid, _, email, isNewUser ->
                busyAction = null
                if (isNewUser) {
                    navController.navigate("SetUsername/$uid/$email?google=true")
                } else {
                    userViewModel.loadUser()
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            },
            onError = { ex ->
                busyAction = null
                val reason = ex.message?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.auth_error_unknown)
                Toast.makeText(
                    context,
                    context.getString(R.string.auth_google_failed_reason, reason),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // --- App language pick (no change) ---
    var currentAppLang by remember { mutableStateOf("en") }
    val availableLanguages = listOf("English", "Deutsch", "Français", "Español", "Português")
    val languageMap = mapOf(
        "en" to "English", "de" to "Deutsch", "fr" to "Français",
        "es" to "Español", "pt" to "Português"
    )
    val reverseMap = remember { languageMap.entries.associate { it.value to it.key } }
    LaunchedEffect(Unit) { userViewModel.getAppLanguage { saved -> currentAppLang = saved } }

    // --- Form state ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.auth_title),
            style = MaterialTheme.typography.h4,
            color = theme.textColor
        )

        Spacer(Modifier.height(16.dp))

        val tfColors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = theme.textColor,
            cursorColor = theme.borderColor,
            focusedBorderColor = theme.borderColor,
            unfocusedBorderColor = theme.textColor.copy(alpha = 0.75f),
            focusedLabelColor = theme.textColor,
            unfocusedLabelColor = theme.textColor.copy(alpha = 0.8f),
            placeholderColor = theme.cardContentColor.copy(alpha = 0.6f),
            trailingIconColor = theme.textColor,
            leadingIconColor = theme.textColor,
            disabledTextColor = theme.textColor.copy(alpha = 0.7f),
            disabledBorderColor = theme.textColor.copy(alpha = 0.4f),
            disabledLabelColor = theme.textColor.copy(alpha = 0.5f)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { if (!isBusy) { email = it; errorMessage = null } },
            enabled = !isBusy,
            label = { Text(stringResource(R.string.auth_email_label), color = theme.textColor.copy(0.9f)) },
            colors = tfColors
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { if (!isBusy) { password = it; errorMessage = null } },
            enabled = !isBusy,
            label = { Text(stringResource(R.string.auth_password_label), color = theme.textColor.copy(0.9f)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                val cd = if (passwordVisible) R.string.auth_toggle_hide else R.string.auth_toggle_show
                IconButton(onClick = { if (!isBusy) passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = stringResource(cd))
                }
            },
            colors = tfColors
        )

        // App language picker
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.settings_app_language_label), color = theme.textColor)
        Spacer(Modifier.height(8.dp))
        LanguageDropdown(
            theme = theme,
            currentCode = currentAppLang,
            availableLanguages = availableLanguages,
            languageMap = languageMap,
            reverseMap = reverseMap
        ) { code ->
            userViewModel.saveAppLanguage(code)
            currentAppLang = code
            applyAppLocale(code)
        }

        Spacer(Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(8.dp))
        }

        // --- Sign in with Email ---
        Button(
            onClick = {
                if (isBusy) return@Button

                val emailT = email.trim()
                val passT = password

                // Validate BEFORE calling Firebase
                when {
                    emailT.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_email)
                        return@Button
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(emailT).matches() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_email)
                        return@Button
                    }
                    passT.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_password)
                        return@Button
                    }
                    passT.length < 6 -> {
                        errorMessage = context.getString(R.string.auth_error_password_weak)
                        return@Button
                    }
                }
                busyAction = AuthBusy.LOGIN
                sessionViewModel.loginWithEmail(
                    email = emailT,
                    password = passT,
                    onSuccess = { uid, _, em, _ ->
                        busyAction = null
                        sessionViewModel.checkEmailVerified(
                            onResult = { verified ->
                                if (!verified) {
                                    // Gate: unverified → Verify screen
                                    navController.navigate(Screen.VerifyEmailScreen.createRoute(uid, em)) {
                                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                    }
                                    return@checkEmailVerified
                                }

                                // Verified → do we already have a public profile?
                                sessionViewModel.checkHasPublicProfile(
                                    uid = uid,
                                    onResult = { hasProfile ->
                                        if (hasProfile) {
                                            // Profile exists → load + go to Main
                                            userViewModel.loadUser()
                                            navController.navigate(Screen.MainScreen.route) {
                                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                            }
                                        } else {
                                            // Verified but no profile → SetUsername
                                            navController.navigate(
                                                Screen.SetUsernameScreen.createRoute(uid, em, isGoogleUser = false)
                                            ) {
                                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                            }
                                        }
                                    },
                                    onFailure = { err ->
                                        // If unsure, send to verify gate to be safe (or show a toast)
                                        navController.navigate(Screen.VerifyEmailScreen.createRoute(uid, em)) {
                                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                        }
                                    }
                                )
                            },
                            onFailure = { err ->
                                // Couldn’t reload — handle gracefully (toast/log), keep them on Login
                                // e.g., Toast.makeText(context, err ?: "Login check failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onFailure = { ex ->
                        busyAction = null
                        val msg = ex.message?.lowercase()
                        errorMessage = when {
                            msg == null -> context.getString(R.string.auth_login_failed)
                            "no user record" in msg -> context.getString(R.string.auth_error_no_user)
                            "password is invalid" in msg -> context.getString(R.string.auth_error_bad_password)
                            "badly formatted" in msg -> context.getString(R.string.auth_error_bad_email)
                            else -> ex.message
                        }
                    }
                )
            },
            enabled = !isBusy,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = theme.buttonBackgroundColor,
                contentColor = theme.textColor,
                disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
                disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
            )
        ) {
            if (busyAction == AuthBusy.LOGIN) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_sign_in_email))
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Register ---
        Button(
            onClick = {
                if (isBusy) return@Button

                val emailT = email.trim()
                val passT = password

                when {
                    emailT.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_email)
                        return@Button
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(emailT).matches() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_email)
                        return@Button
                    }
                    passT.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_bad_password)
                        return@Button
                    }
                    passT.length < 6 -> {
                        errorMessage = context.getString(R.string.auth_error_password_weak)
                        return@Button
                    }
                }
                busyAction = AuthBusy.REGISTER
                sessionViewModel.registerWithEmail(
                    email = emailT,
                    password = passT,
                    onSuccess = { uid, _, em, isNewUser ->
                        busyAction = null
                        if (isNewUser) {
                            sessionViewModel.sendEmailVerification(onSuccess = {}, onFailure = {})
                            navController.navigate(Screen.VerifyEmailScreen.createRoute(uid, em)) {
                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                            }
                        } else {
                            // optional: gate even on existing
                            sessionViewModel.checkEmailVerified(
                                onResult = { verified ->
                                    if (verified) {
                                        // proceed to Main or SetUsername
                                    } else {
                                        // go to VerifyEmail screen
                                    }
                                },
                                onFailure = { err ->
                                    // show toast / log error
                                }
                            )
                        }
                    },
                    onFailure = { ex ->
                        busyAction = null
                        val msg = ex.message?.lowercase().orEmpty()
                        errorMessage = when {
                            msg.isBlank() -> context.getString(R.string.auth_registration_failed)
                            "already in use" in msg -> context.getString(R.string.auth_error_email_in_use)
                            "badly formatted" in msg -> context.getString(R.string.auth_error_bad_email)
                            "password should be at least" in msg -> context.getString(R.string.auth_error_password_weak)
                            else -> ex.message
                        }
                    }
                )
            },
            enabled = !isBusy,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = theme.buttonBackgroundColor,
                contentColor = theme.textColor,
                disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
                disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
            )
        ) {
            if (busyAction == AuthBusy.REGISTER) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_register), color = theme.textColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Google Sign-In ---
        Button(
            onClick = {
                if (isBusy) return@Button
                busyAction = AuthBusy.GOOGLE
                launcher.launch(googleSignInClient.signInIntent)
            },
            enabled = !isBusy,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = theme.buttonBackgroundColor,
                contentColor = theme.textColor,
                disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
                disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
            )
        ) {
            if (busyAction == AuthBusy.GOOGLE) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_sign_in_google), color = theme.textColor)
            }
        }
    }
}
