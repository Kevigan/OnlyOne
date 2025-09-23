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

// Tracks which action is currently busy (to disable UI and show spinners)
enum class AuthBusy { LOGIN, REGISTER, GOOGLE }

@Composable
fun LoginView(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel,
    theme: ThemeTokens
) {
    val context = LocalContext.current

    // ────────────────────────────────────────────────────────────────────────────
    // Google Sign-In setup
    // ────────────────────────────────────────────────────────────────────────────
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Busy flag for buttons/spinners
    var busyAction by remember { mutableStateOf<AuthBusy?>(null) }
    val isBusy = busyAction != null

    // Navigation helpers (keeps code paths tidy)
    fun goToAgeGate(uid: String, email: String, isGoogle: Boolean) {
        navController.navigate(Screen.AgeGateScreen.createRoute(uid, email, isGoogle)) {
            popUpTo(Screen.LoginScreen.route) { inclusive = true }
        }
    }
    fun goToSetUsername(uid: String, email: String, isGoogle: Boolean) {
        navController.navigate(Screen.SetUsernameScreen.createRoute(uid, email, isGoogleUser = isGoogle)) {
            popUpTo(Screen.LoginScreen.route) { inclusive = true }
        }
    }
    fun goToMain() {
        navController.navigate(Screen.MainScreen.route) {
            popUpTo(Screen.LoginScreen.route) { inclusive = true }
        }
    }
    fun goToVerify(uid: String, email: String) {
        navController.navigate(Screen.VerifyEmailScreen.createRoute(uid, email)) {
            popUpTo(Screen.LoginScreen.route) { inclusive = true }
        }
    }

    // Handle Google result
    // NEW: Only NEW Google users → AgeGate; EXISTING → Main or SetUsername
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sessionViewModel.handleGoogleSignInResult(
            resultData = result.data,
            onSuccess = { uid, _, email, isNewUser ->
                busyAction = null
                if (isNewUser) {
                    // First-time Google sign-in → AgeGate flow
                    goToAgeGate(uid, email, isGoogle = true)
                } else {
                    // Existing Google account → skip AgeGate
                    sessionViewModel.checkHasPublicProfile(
                        uid = uid,
                        onResult = { hasProfile ->
                            if (hasProfile) {
                                userViewModel.loadUser()
                                goToMain()
                            } else {
                                goToSetUsername(uid, email, isGoogle = true)
                            }
                        },
                        onFailure = {
                            // If the check fails, prefer sending to SetUsername
                            goToSetUsername(uid, email, isGoogle = true)
                        }
                    )
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

    // ────────────────────────────────────────────────────────────────────────────
    // App language picker state (unchanged logic)
    // ────────────────────────────────────────────────────────────────────────────
    var currentAppLang by remember { mutableStateOf("en") }
    val availableLanguages = listOf("English", "Deutsch", "Français", "Español", "Português")
    val languageMap = mapOf(
        "en" to "English", "de" to "Deutsch", "fr" to "Français",
        "es" to "Español", "pt" to "Português"
    )
    val reverseMap = remember { languageMap.entries.associate { it.value to it.key } }
    LaunchedEffect(Unit) { userViewModel.getAppLanguage { saved -> currentAppLang = saved } }

    // ────────────────────────────────────────────────────────────────────────────
    // Form state & colors
    // ────────────────────────────────────────────────────────────────────────────
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonBackgroundColor,
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )

    // ────────────────────────────────────────────────────────────────────────────
    // UI
    // ────────────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.h4,
            color = theme.textColor
        )

        Spacer(Modifier.height(16.dp))

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

        // Language picker
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

        // ────────────────────────────────────────────────────────────────────────
        // Email: Sign In
        // ────────────────────────────────────────────────────────────────────────
        Button(
            onClick = {
                if (isBusy) return@Button
                val emailT = email.trim()
                val passT = password

                // Validate BEFORE calling Firebase
                when {
                    emailT.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailT).matches() ->
                        context.getString(R.string.auth_error_bad_email).also { errorMessage = it; return@Button }
                    passT.isBlank() ->
                        context.getString(R.string.auth_error_bad_password).also { errorMessage = it; return@Button }
                    passT.length < 6 ->
                        context.getString(R.string.auth_error_password_weak).also { errorMessage = it; return@Button }
                }

                busyAction = AuthBusy.LOGIN
                sessionViewModel.loginWithEmail(
                    email = emailT,
                    password = passT,
                    onSuccess = { uid, _, em, _ ->
                        busyAction = null
                        // Email sign-in requires verification → Verify gate first
                        sessionViewModel.checkEmailVerified(
                            onResult = { verified ->
                                if (!verified) {
                                    goToVerify(uid, em)
                                    return@checkEmailVerified
                                }
                                // Verified → profile gate
                                sessionViewModel.checkHasPublicProfile(
                                    uid = uid,
                                    onResult = { hasProfile ->
                                        if (hasProfile) {
                                            userViewModel.loadUser()
                                            goToMain()
                                        } else {
                                            goToSetUsername(uid, em, isGoogle = false)
                                        }
                                    },
                                    onFailure = {
                                        // If unsure, send to Verify gate (conservative)
                                        goToVerify(uid, em)
                                    }
                                )
                            },
                            onFailure = {
                                // Couldn’t reload — keep them on Login (optional toast/log)
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
            colors = buttonColors
        ) {
            if (busyAction == AuthBusy.LOGIN) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_sign_in_email))
            }
        }

        Spacer(Modifier.height(8.dp))

        // ────────────────────────────────────────────────────────────────────────
        // Email: Register
        // ────────────────────────────────────────────────────────────────────────
        Button(
            onClick = {
                if (isBusy) return@Button
                val emailT = email.trim()
                val passT = password

                when {
                    emailT.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailT).matches() ->
                        context.getString(R.string.auth_error_bad_email).also { errorMessage = it; return@Button }
                    passT.isBlank() ->
                        context.getString(R.string.auth_error_bad_password).also { errorMessage = it; return@Button }
                    passT.length < 6 ->
                        context.getString(R.string.auth_error_password_weak).also { errorMessage = it; return@Button }
                }

                busyAction = AuthBusy.REGISTER
                sessionViewModel.registerWithEmail(
                    email = emailT,
                    password = passT,
                    onSuccess = { uid, _, em, isNewUser ->
                        busyAction = null
                        if (isNewUser) {
                            // New email account → send verify and go to Verify gate
                            sessionViewModel.sendEmailVerification(onSuccess = {}, onFailure = {})
                            goToVerify(uid, em)
                        } else {
                            // Rare path (register called on existing email):
                            // Optionally enforce verify and profile gate
                            sessionViewModel.checkEmailVerified(
                                onResult = { verified ->
                                    if (verified) {
                                        sessionViewModel.checkHasPublicProfile(
                                            uid = uid,
                                            onResult = { hasProfile ->
                                                if (hasProfile) {
                                                    userViewModel.loadUser()
                                                    goToMain()
                                                } else {
                                                    goToSetUsername(uid, em, isGoogle = false)
                                                }
                                            },
                                            onFailure = { goToVerify(uid, em) }
                                        )
                                    } else {
                                        goToVerify(uid, em)
                                    }
                                },
                                onFailure = { /* stay here; toast/log if desired */ }
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
            colors = buttonColors
        ) {
            if (busyAction == AuthBusy.REGISTER) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_register), color = theme.textColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ────────────────────────────────────────────────────────────────────────
        // Google Sign-In
        // ────────────────────────────────────────────────────────────────────────
        Button(
            onClick = {
                if (isBusy) return@Button
                busyAction = AuthBusy.GOOGLE
                launcher.launch(googleSignInClient.signInIntent)
            },
            enabled = !isBusy,
            colors = buttonColors
        ) {
            if (busyAction == AuthBusy.GOOGLE) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.auth_sign_in_google), color = theme.textColor)
            }
        }
    }
}
