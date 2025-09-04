package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.data.PublicUser
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
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

    suspend fun updateUserPublicProfileSecure(
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("updatePublicProfileFields")
                .call(mapOf("updates" to updates))
                .await()
            onSuccess()
        } catch (e: Exception) {
            Log.e("UserPublicRepo", "❌ updateUserPublicProfileSecure failed", e)
            onFailure(e)
        }
    }

    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        //trackRead("users_public/$uid", "getPublicUser")
        return db.collection("users_public").document(uid).get()
    }

    suspend fun getPublicUserSuspend(uid: String): PublicUser {
        val doc = db.collection("users_public").document(uid).get().await()
        return doc.toObject(PublicUser::class.java) ?: throw Exception("Invalid user_public/$uid")
    }

    // UserPublicRepo.kt
    suspend fun setFavouriteMessage(
        messageId: String,
        text: String,
        fromUid: String,
        senderUsername: String? = null,
       // senderAvatarId: Int? = null,
        senderMood: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val data = hashMapOf(
                "messageId" to messageId,
                "text" to text,
                "fromUid" to fromUid,
                "senderUsername" to (senderUsername ?: "")
            ).apply {
                // both exist in your LocalFavoriteMessage; include them if present
                //senderAvatarId?.let { put("senderAvatarId", it) }
                senderMood?.let { put("senderMood", it) }
            }

            Firebase.functions("europe-west3")
                .getHttpsCallable("setFavouriteMessage")
                .call(data)
                .await()

            trackWrite("users_public/<uid>.favouriteMessage", "setFavouriteMessage")
            onSuccess()
        } catch (e: Exception) {
            Log.e("UserPublicRepo", "❌ setFavouriteMessage failed", e)
            onFailure(e)
        }
    }


    suspend fun clearFavouriteMessage(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            Firebase.functions("europe-west3")
                .getHttpsCallable("clearFavouriteMessage")
                .call()
                .await()
            trackWrite("users_public/<uid>.favouriteMessage=null", "clearFavouriteMessage")
            onSuccess()
        } catch (e: Exception) {
            Log.e("UserPublicRepo", "❌ clearFavouriteMessage failed", e)
            onFailure(e)
        }
    }

}