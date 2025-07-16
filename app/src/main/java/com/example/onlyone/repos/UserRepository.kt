package com.example.onlyone.repos

import com.example.onlyone.data.PrivateUser
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val db: FirebaseFirestore) {
    fun createUserProfile(uid: String, email: String, username: String): Task<Void> {
        // Public User object
        val publicUser = PublicUser(
            uid = uid,
            username = username,
            moodStatus = "",
            avatarId = 0,
            points = 0
        )

        // Private User object
        val privateUser = PrivateUser(
            uid = uid,
            email = email,
            blockList = emptyList(),
            reportCount = 0,
            isPro = false,
            friendList = emptyList(),
            incomingFriendRequests = emptyList(),
            outgoingFriendRequests = emptyList()
        )

        // Create references for public and private users
        val publicRef = db.collection("users_public").document(uid)
        val privateRef = db.collection("users_private").document(uid)

        // Batch writing for both public and private data
        val batch = db.batch()
        batch.set(publicRef, publicUser)
        batch.set(privateRef, privateUser)

        return batch.commit()
    }
    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        return db.collection("users_public").document(uid).get()
    }

    fun getPrivateUser(uid: String): Task<DocumentSnapshot> {
        return db.collection("users_private").document(uid).get()
    }

    fun getFullUser(uid: String, onComplete: (User?) -> Unit) {
        // Updated paths for flattened structure
        val publicRef = db.collection("users_public").document(uid)
        val privateRef = db.collection("users_private").document(uid)

        // Fetch public data
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
                        reportCount = private.reportCount,
                        friendList = private.friendList, // Set friendList from private data
                        incomingFriendRequests = private.incomingFriendRequests, // Set incoming requests from private data
                        outgoingFriendRequests = private.outgoingFriendRequests // Set outgoing requests from private data
                    )
                    onComplete(user)
                } else {
                    onComplete(null) // If either public or private data is null
                }
            }.addOnFailureListener {
                onComplete(null) // Handle failure when fetching private data
            }
        }.addOnFailureListener {
            onComplete(null) // Handle failure when fetching public data
        }
    }

    fun isUsernameTaken(username: String, onResult: (Boolean) -> Unit) {
        val usersRef = db.collection("users")
        usersRef
            .whereEqualTo("public.data.username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onResult(!querySnapshot.isEmpty)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun createUser(user: User): Task<Void> {
        return db.collection("users_public").document(user.uid).set(user)
    }

    fun updateMood(uid: String, mood: String): Task<Void> {
        return db.collection("users_public").document(uid).update("moodStatus", mood)
    }

    fun blockUser(currentUid: String, blockedUid: String): Task<Void> {
        return db.collection("users_private").document(currentUid)
            .update("blockList", FieldValue.arrayUnion(blockedUid))
    }

    fun findUserByEmail(email: String, onResult: (String?) -> Unit) {
        db.collection("users_private")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val uid = querySnapshot.documents.firstOrNull()?.id
                onResult(uid) // null if not found
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getPublicUsers(uids: List<String>, onComplete: (List<PublicUser>) -> Unit) {
        if (uids.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val chunks = uids.chunked(10) // Split into chunks of 10
        val results = mutableListOf<PublicUser>()

        val tasks = chunks.map { chunk ->
            db.collection("users_public")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
        }

        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                snapshots.forEach { snapshot ->
                    snapshot.documents.forEach { doc ->
                        doc.toObject(PublicUser::class.java)?.let { results.add(it) }
                    }
                }
                onComplete(results)
            }
            .addOnFailureListener {
                onComplete(emptyList()) // Optionally handle errors more gracefully
            }
    }

    fun sendFriendRequest(fromUid: String, toUid: String): Task<Void> {
        val fromRef = db.collection("users_private").document(fromUid)
        val toRef = db.collection("users_private").document(toUid)

        val batch = db.batch()
        batch.update(fromRef, "outgoingFriendRequests", FieldValue.arrayUnion(toUid))
        batch.update(toRef, "incomingFriendRequests", FieldValue.arrayUnion(fromUid))
        return batch.commit()
    }

    fun acceptFriendRequest(currentUid: String, requesterUid: String): Task<Void> {
        val currentRef = db.collection("users_private").document(currentUid)
        val requesterRef = db.collection("users_private").document(requesterUid)

        val batch = db.batch()
        batch.update(currentRef, "friendList", FieldValue.arrayUnion(requesterUid))
        batch.update(requesterRef, "friendList", FieldValue.arrayUnion(currentUid))

        batch.update(currentRef, "incomingFriendRequests", FieldValue.arrayRemove(requesterUid))
        batch.update(requesterRef, "outgoingFriendRequests", FieldValue.arrayRemove(currentUid))
        return batch.commit()
    }

    // Add more: reportUser(), updatePoints(), etc.
}
