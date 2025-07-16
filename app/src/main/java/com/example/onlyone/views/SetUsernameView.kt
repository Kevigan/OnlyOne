package com.example.onlyone.views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import com.example.onlyone.viewModels.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SetUsernameView(
    uid: String,
    email: String,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose your username")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isBlank()) {
                    Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isSaving = true

                val db = FirebaseFirestore.getInstance()

                // 🔍 Check if username already exists
                db.collection("users_public")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            Toast.makeText(context, "Username is already taken", Toast.LENGTH_SHORT).show()
                            isSaving = false
                        } else {
                            // Call the `createUserProfile` function via `UserRepository`
                            userViewModel.repository.createUserProfile(uid, email, username)
                                .addOnSuccessListener {
                                    userViewModel.loadUser(uid)
                                    navController.navigate(Screen.MainScreen.route) {
                                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to save user profile", Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error checking username", Toast.LENGTH_SHORT).show()
                        isSaving = false
                    }
            },
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Continue")
        }
    }
}

