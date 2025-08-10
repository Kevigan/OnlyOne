package com.example.onlyone.views

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.onlyone.BuildConfig
import com.example.onlyone.Screen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.example.onlyone.R
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.views.settingsView.LanguageDropdown

@Composable
fun LoginView(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.WEB_CLIENT_ID)
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sessionViewModel.handleGoogleSignInResult(
            resultData = result.data,
            onSuccess = { uid, name, email, isNewUser ->
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

    // --- NEW: App language state + maps (reuse the same ones from LanguageSettingsView) ---
    var currentAppLang by remember { mutableStateOf("en") }
    val availableLanguages = listOf("English", "Deutsch", "Français", "Español", "Português")
    val languageMap = mapOf(
        "en" to "English",
        "de" to "Deutsch",
        "fr" to "Français",
        "es" to "Español",
        "pt" to "Português"
    )
    val reverseMap = languageMap.entries.associate { it.value to it.key }

    // Load saved app language just to display the current selection (do NOT apply here)
    LaunchedEffect(Unit) {
        userViewModel.getAppLanguage { saved -> currentAppLang = saved }
    }
    // --- END new ---

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
        Text(stringResource(R.string.auth_title), style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.auth_email_label)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.auth_password_label)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                val cd   = if (passwordVisible) R.string.auth_toggle_hide else R.string.auth_toggle_show
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = stringResource(cd))
                }
            }
        )

        // --- NEW: App Language section (uses your LanguageDropdown) ---
        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.settings_app_language_label))
        Spacer(modifier = Modifier.height(8.dp))

        LanguageDropdown(
            currentCode = currentAppLang,
            availableLanguages = availableLanguages,
            languageMap = languageMap,
            reverseMap = reverseMap
        ) { code ->
            // Persist + apply immediately
            userViewModel.saveAppLanguage(code)
            currentAppLang = code
            applyAppLocale(code)
        }
        // --- END new ---

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            sessionViewModel.loginWithEmail(
                email = email,
                password = password,
                onSuccess = { uid, name, email, isNewUser ->
                    if (isNewUser) {
                        navController.navigate("SetUsername/$uid/$email")
                    } else {
                        userViewModel.loadUser()
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                onFailure = { ex ->
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
        }) {
            Text(stringResource(R.string.auth_sign_in_email))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            sessionViewModel.registerWithEmail(
                email = email,
                password = password,
                onSuccess = { uid, name, email, isNewUser ->
                    if (isNewUser) {
                        navController.navigate("SetUsername/$uid/$email")
                    } else {
                        userViewModel.loadUser()
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                onFailure = { ex ->
                    val msg = ex.message?.lowercase().orEmpty()
                    errorMessage = when {
                        msg.isBlank() ->
                            context.getString(R.string.auth_registration_failed)
                        "already in use" in msg ->
                            context.getString(R.string.auth_error_email_in_use)
                        "badly formatted" in msg ->
                            context.getString(R.string.auth_error_bad_email)
                        "password should be at least" in msg ->
                            context.getString(R.string.auth_error_password_weak)
                        else -> ex.message
                    }
                }
            )
        }) {
            Text(stringResource(R.string.auth_register))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }) {
            Text(stringResource(R.string.auth_sign_in_google))
        }
    }
}



