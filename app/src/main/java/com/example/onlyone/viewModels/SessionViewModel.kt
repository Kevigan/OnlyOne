package com.example.onlyone.viewModels

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.onlyone.data.PrivateUser
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
        }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: (String, String, String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser!!
                onSuccess(user.uid, user.displayName ?: email.substringBefore("@"), user.email ?: email)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: (String, String, String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener
                val displayName = email.substringBefore("@")

                val publicUser = PublicUser(
                    uid = user.uid,
                    username = displayName
                )

                val privateUser = PrivateUser(
                    uid = user.uid,
                    email = email
                )

                // Save both
                val batch = firestore.batch()
                val publicRef = firestore.collection("users").document(user.uid).collection("public").document("data")
                val privateRef = firestore.collection("users").document(user.uid).collection("private").document("data")

                batch.set(publicRef, publicUser)
                batch.set(privateRef, privateUser)

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess(user.uid, displayName, email)
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun handleGoogleSignInResult(
        resultData: Intent?,
        onSuccess: (String, String, String) -> Unit,
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

                    val publicRef = firestore.collection("users").document(uid).collection("public").document("data")

                    publicRef.get().addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            val publicUser = PublicUser(uid = uid, username = displayName)
                            val privateUser = PrivateUser(uid = uid, email = email)

                            val batch = firestore.batch()
                            batch.set(publicRef, publicUser)
                            batch.set(firestore.collection("users").document(uid).collection("private").document("data"), privateUser)

                            batch.commit()
                                .addOnSuccessListener {
                                    onSuccess(uid, displayName, email)
                                }
                                .addOnFailureListener { onError(it) }
                        } else {
                            onSuccess(uid, displayName, email)
                        }
                    }.addOnFailureListener { onError(it) }

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
}
