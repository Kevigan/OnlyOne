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
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.viewModels.SessionViewModel
import com.example.onlyone.viewModels.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginView(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("YOUR_WEB_CLIENT_ID") // 🔒 Replace with real one
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        sessionViewModel.handleGoogleSignInResult(
            resultData = result.data,
            onSuccess = { uid, name, email ->
                userViewModel.loadUser(uid)
                navController.navigate("MainScreen") {
                    popUpTo("LoginScreen") { inclusive = true }
                }
            },
            onError = {
                Toast.makeText(context, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            sessionViewModel.loginWithEmail(
                email = email,
                password = password,
                onSuccess = { uid, name, email ->
                    userViewModel.loadUser(uid)
                    navController.navigate("MainScreen") {
                        popUpTo("LoginScreen") { inclusive = true }
                    }
                },
                onFailure = {
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
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
                onSuccess = { uid, name, email ->
                    userViewModel.loadUser(uid)
                    navController.navigate("MainScreen") {
                        popUpTo("LoginScreen") { inclusive = true }
                    }
                },
                onFailure = {
                    Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
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
