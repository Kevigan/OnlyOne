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
import com.example.onlyone.viewModels.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.onlyone.BuildConfig
import com.example.onlyone.Screen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

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
                    navController.navigate("SetUsername/$uid/$email")
                } else {
                    userViewModel.loadUser(uid)
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            },
            onError = {
                Toast.makeText(context, "Google Sign-in failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

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
        Text("Login", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide" else "Show")
                }
            }
        )

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
                        userViewModel.loadUser(uid)
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                onFailure = {
                    errorMessage = when (it.message?.lowercase()) {
                        null -> "Login failed"
                        else -> {
                            when {
                                "no user record" in it.message!!.lowercase() -> "No account found for this email."
                                "password is invalid" in it.message!!.lowercase() -> "Incorrect password."
                                "badly formatted" in it.message!!.lowercase() -> "Invalid email format."
                                else -> it.message
                            }
                        }
                    }
                }
            )
        }) {
            Text("Sign In with Email")
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
                        userViewModel.loadUser(uid)
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                onFailure = {
                    errorMessage = when (it.message?.lowercase()) {
                        null -> "Registration failed"
                        else -> {
                            when {
                                "already in use" in it.message!! -> "An account with this email already exists."
                                "badly formatted" in it.message!! -> "Invalid email format."
                                "password should be at least" in it.message!! -> "Password is too weak."
                                else -> it.message
                            }
                        }
                    }
                }
            )
        }) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }) {
            Text("Sign in with Google")
        }
    }
}


