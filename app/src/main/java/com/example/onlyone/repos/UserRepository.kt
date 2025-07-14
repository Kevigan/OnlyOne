package com.example.onlyone.repos

import com.example.onlyone.data.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(/*private val db: FirebaseFirestore*/) {

   /* fun getUser(uid: String): Task<DocumentSnapshot> {
        return db.collection("users").document(uid).get()
    }

    fun createUser(user: User): Task<Void> {
        return db.collection("users").document(user.uid).set(user)
    }

    fun updateMood(uid: String, mood: String): Task<Void> {
        return db.collection("users").document(uid).update("moodStatus", mood)
    }

    fun blockUser(currentUid: String, blockedUid: String): Task<Void> {
        return db.collection("users").document(currentUid)
            .update("blockList", FieldValue.arrayUnion(blockedUid))
    }*/

    // Add more: reportUser(), updatePoints(), etc.
}
