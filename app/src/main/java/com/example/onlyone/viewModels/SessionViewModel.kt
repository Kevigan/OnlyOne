package com.example.onlyone.viewModels

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.onlyone.repos.userRepos.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
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
                val displayName = email.substringBefore("@")
                val isNewUser = true

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        userRepository.createUserProfile(
                            email = email,
                            username = displayName,
                            fcmToken = token,
                            chatLanguage = "any",
                            onSuccess = {
                                onSuccess(uid, displayName, email, isNewUser)
                            },
                            onFailure = { exception ->
                                onFailure(exception)
                            }
                        )
                    }
                    .addOnFailureListener { tokenError ->
                        // fallback: continue without token
                        userRepository.createUserProfile(
                            email = email,
                            username = displayName,
                            fcmToken = null,
                            chatLanguage = "any",
                            onSuccess = {
                                onSuccess(uid, displayName, email, isNewUser)
                            },
                            onFailure = { exception ->
                                onFailure(exception)
                            }
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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
                        userRepository.syncFcmToken()
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
                            userRepository.syncFcmToken()
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

    fun signOut(googleSignInClient: GoogleSignInClient? = null) {
        auth.signOut()
        googleSignInClient?.signOut()
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure(IllegalStateException("No user"))
        user.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { ex -> onFailure(ex) }
    }


    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun changePasswordWithCurrentPassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure(IllegalStateException("Not signed in"))
        val email = user.email ?: return onFailure(IllegalStateException("No email on account"))

        // 1) Re-authenticate
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // 2) Update password
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun changePasswordAfterReauth(
        newPassword: String,
        credential: AuthCredential,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure(IllegalStateException("Not signed in"))
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }


    fun reauthAndDeleteWithPassword(
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure(IllegalStateException("No user"))
        val email = user.email ?: return onFailure(IllegalStateException("No email for user"))

        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}


