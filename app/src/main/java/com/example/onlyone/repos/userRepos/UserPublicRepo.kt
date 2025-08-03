package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.data.PublicUser
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserPublicRepo @Inject constructor(
    private val db: FirebaseFirestore,
) {
    private var readCount = 0
    private var writeCount = 0
    fun trackWrite(path: String, from: String = "") {
        writeCount++
        Log.d(
            "FirestoreTrack",
            "Write from UserRepo [$writeCount]: $path${if (from.isNotBlank()) " ($from)" else ""}"
        )
    }
    fun updateMood(uid: String, mood: String): Task<Void> {
        //trackWrite("users_public/$uid → moodStatus", "updateMood")
        return db.collection("users_public").document(uid).update("moodStatus", mood)
    }

    fun updateChatLanguage(uid: String, language: String): Task<Void> {
        return db.collection("users_public").document(uid).update("chatLanguage", language)
    }

    fun updateUserPublicProfile(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users_public").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        //trackRead("users_public/$uid", "getPublicUser")
        return db.collection("users_public").document(uid).get()
    }

    suspend fun getPublicUserSuspend(uid: String): PublicUser {
        val doc = db.collection("users_public").document(uid).get().await()
        return doc.toObject(PublicUser::class.java) ?: throw Exception("Invalid user_public/$uid")
    }

}