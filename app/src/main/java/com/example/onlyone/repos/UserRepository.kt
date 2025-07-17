package com.example.onlyone.repos

import android.util.Log
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
    private val publicUserCache = mutableMapOf<String, PublicUser>()

    private var readCount = 0
    private var writeCount = 0

    fun trackRead(path: String) {
        readCount++
        Log.d("FirestoreTrack", "Read [$readCount]: $path")
    }

    fun trackWrite(path: String) {
        writeCount++
        Log.d("FirestoreTrack", "Write [$writeCount]: $path")
    }


    fun createUserProfile(uid: String, email: String, username: String): Task<Void> {
        trackWrite("users_public/$uid (new user)")
        trackWrite("users_private/$uid (new user)")

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
        trackRead("users_public/$uid")
        return db.collection("users_public").document(uid).get()
    }

    fun getPrivateUser(uid: String): Task<DocumentSnapshot> {
        trackRead("users_private/$uid")
        return db.collection("users_private").document(uid).get()
    }

    fun getFullUser(uid: String, onComplete: (User?) -> Unit) {
        trackRead("users_public/$uid")
        trackRead("users_private/$uid")

        val publicTask = db.collection("users_public").document(uid).get()
        val privateTask = db.collection("users_private").document(uid).get()

        Tasks.whenAllSuccess<DocumentSnapshot>(listOf(publicTask, privateTask))
            .addOnSuccessListener { docs ->
                val publicSnap = docs[0] as DocumentSnapshot
                val privateSnap = docs[1] as DocumentSnapshot

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
                        friendList = private.friendList,
                        incomingFriendRequests = private.incomingFriendRequests,
                        outgoingFriendRequests = private.outgoingFriendRequests
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
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
        trackWrite("users_public/$uid → moodStatus")
        return db.collection("users_public").document(uid).update("moodStatus", mood)
    }

    fun blockUser(currentUid: String, blockedUid: String): Task<Void> {
        trackWrite("users_private/$currentUid → blockList")
        return db.collection("users_private").document(currentUid)
            .update("blockList", FieldValue.arrayUnion(blockedUid))
    }

    fun findUserByEmail(email: String, onResult: (String?) -> Unit) {
        trackRead("users_private?email=$email")
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

        val (cached, toFetch) = uids.partition { publicUserCache.containsKey(it) }

        val cachedUsers = cached.mapNotNull { publicUserCache[it] }

        if (toFetch.isEmpty()) {
            onComplete(cachedUsers)
            return
        }

        val chunks = toFetch.chunked(10)
        val results = mutableListOf<PublicUser>()

        val tasks = chunks.map { chunk ->
            chunk.forEach { trackRead("users_public/$it") }
            db.collection("users_public")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
        }

        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                snapshots.forEach { snapshot ->
                    Log.d("FirestoreTrack", "→ Chunk returned ${snapshot.size()} docs")
                    snapshot.documents.forEach { doc ->
                        doc.toObject(PublicUser::class.java)?.let {
                            publicUserCache[it.uid] = it
                            results.add(it)
                        }
                    }
                }
                onComplete(cachedUsers + results)
            }
            .addOnFailureListener {
                onComplete(cachedUsers) // fallback to cached only
            }
    }

    fun sendFriendRequest(fromUid: String, toUid: String): Task<Void> {
        trackWrite("users_private/$fromUid → outgoingFriendRequests")
        trackWrite("users_private/$toUid → incomingFriendRequests")

        val fromRef = db.collection("users_private").document(fromUid)
        val toRef = db.collection("users_private").document(toUid)

        val batch = db.batch()
        batch.update(fromRef, "outgoingFriendRequests", FieldValue.arrayUnion(toUid))
        batch.update(toRef, "incomingFriendRequests", FieldValue.arrayUnion(fromUid))
        return batch.commit()
    }

    fun cancelOutgoingFriendRequest(fromUid: String, toUid: String): Task<Void> {
        trackWrite("users_private/$fromUid → outgoingFriendRequests (remove)")
        trackWrite("users_private/$toUid → incomingFriendRequests (remove)")

        val fromRef = db.collection("users_private").document(fromUid)
        val toRef = db.collection("users_private").document(toUid)

        val batch = db.batch()
        batch.update(fromRef, "outgoingFriendRequests", FieldValue.arrayRemove(toUid))
        batch.update(toRef, "incomingFriendRequests", FieldValue.arrayRemove(fromUid))

        return batch.commit()
    }


    fun acceptFriendRequest(currentUid: String, requesterUid: String): Task<Void> {
        trackWrite("users_private/$currentUid → friendList, incomingFriendRequests")
        trackWrite("users_private/$requesterUid → friendList, outgoingFriendRequests")

        val currentRef = db.collection("users_private").document(currentUid)
        val requesterRef = db.collection("users_private").document(requesterUid)

        val batch = db.batch()
        batch.update(currentRef, "friendList", FieldValue.arrayUnion(requesterUid))
        batch.update(requesterRef, "friendList", FieldValue.arrayUnion(currentUid))

        batch.update(currentRef, "incomingFriendRequests", FieldValue.arrayRemove(requesterUid))
        batch.update(requesterRef, "outgoingFriendRequests", FieldValue.arrayRemove(currentUid))
        return batch.commit()
    }

    fun declineFriendRequest(currentUid: String, requesterUid: String): Task<Void> {
        trackWrite("users_private/$currentUid → incomingFriendRequests (remove)")
        trackWrite("users_private/$requesterUid → outgoingFriendRequests (remove)")

        val currentRef = db.collection("users_private").document(currentUid)
        val requesterRef = db.collection("users_private").document(requesterUid)

        val batch = db.batch()
        batch.update(currentRef, "incomingFriendRequests", FieldValue.arrayRemove(requesterUid))
        batch.update(requesterRef, "outgoingFriendRequests", FieldValue.arrayRemove(currentUid))
        return batch.commit()
    }


    // Add more: reportUser(), updatePoints(), etc.
}
