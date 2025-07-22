package com.example.onlyone.viewModels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.onlyone.data.PrivateUser
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.example.onlyone.repos.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
        }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: (String, String, String, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener
                val uid = user.uid
                val displayName = email.substringBefore("@") // default display name from email
                val isNewUser = true // As this is a new registration

                // Delegate user creation to UserRepository
                userRepository.createUserProfile(uid, email, displayName)
                    .addOnSuccessListener {
                        syncFcmToken()
                        onSuccess(uid, displayName, email, isNewUser)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception) // Handle errors from UserRepository
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Handle errors from Firebase Auth registration
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: (String, String, String, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser!!
                val uid = user.uid
                val displayName = user.displayName ?: email.substringBefore("@")

                firestore.collection("users_public").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val isNewUser = !doc.exists()
                        syncFcmToken()
                        onSuccess(uid, displayName, email, isNewUser)
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun handleGoogleSignInResult(
        resultData: Intent?,
        onSuccess: (String, String, String, Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(resultData)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val user = auth.currentUser!!
                    val uid = user.uid
                    val displayName = user.displayName ?: ""
                    val email = user.email ?: ""

                    firestore.collection("users_public").document(uid).get()
                        .addOnSuccessListener { doc ->
                            syncFcmToken()
                            val isNewUser = !doc.exists()

                            // ✅ Just report isNewUser, don't create anything here
                            onSuccess(uid, displayName, email, isNewUser)
                        }
                        .addOnFailureListener { onError(it) }
                }
                .addOnFailureListener { onError(it) }

        } catch (e: ApiException) {
            onError(e)
        }
    }

    fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "❌ Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUid = auth.currentUser?.uid

            if (currentUid != null && token != null) {
                firestore.collection("users_private")
                    .document(currentUid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "✅ Token saved to Firestore: $token")
                    }
                    .addOnFailureListener {
                        Log.e("FCM", "❌ Failed to save token", it)
                    }
            }
        }
    }


    fun signOut(googleSignInClient: GoogleSignInClient? = null) {
        auth.signOut()
        googleSignInClient?.signOut()
    }
}


