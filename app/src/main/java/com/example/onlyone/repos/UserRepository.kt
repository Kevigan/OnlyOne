package com.example.onlyone.repos

import com.example.onlyone.data.PrivateUser
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val db: FirebaseFirestore) {

    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        return db.collection("users").document(uid).get()
    }

    fun getPrivateUser(uid: String): Task<DocumentSnapshot> {
        return db.collection("users_private").document(uid).get()
    }

    fun getFullUser(uid: String, onComplete: (User?) -> Unit) {
        val publicRef = db.collection("users").document(uid)
        val privateRef = db.collection("users_private").document(uid)

        publicRef.get().addOnSuccessListener { publicSnap ->
            privateRef.get().addOnSuccessListener { privateSnap ->
                val public = publicSnap.toObject(PublicUser::class.java)
                val private = privateSnap.toObject(PrivateUser::class.java)

                if (public != null && private != null) {
                    val user = User(
                        uid = public.uid,
                        username = public.username,
                        moodStatus = public.moodStatus,
                        avatarId = public.avatarId,
                        points = public.points,
                        email = private.email,
                        blockList = private.blockList,
                        reportCount = private.reportCount
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
        }
    }


    fun createUser(user: User): Task<Void> {
        return db.collection("users").document(user.uid).set(user)
    }

    fun updateMood(uid: String, mood: String): Task<Void> {
        return db.collection("users").document(uid).update("moodStatus", mood)
    }

    fun blockUser(currentUid: String, blockedUid: String): Task<Void> {
        return db.collection("users_private").document(currentUid)
            .update("blockList", FieldValue.arrayUnion(blockedUid))
    }

    // Add more: reportUser(), updatePoints(), etc.
}
