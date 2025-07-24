package com.example.onlyone.repos

import android.util.Log
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.SwipeDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.LocalSwipeStatus
import com.example.onlyone.data.PrivateUser
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.example.onlyone.data.UserSwipeStatus
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val friendDao: FriendDao,
    private val messageDao: MessageDao,
    private val swipeDao: SwipeDao
) {
    private val publicUserCache = mutableMapOf<String, PublicUser>()

    private var readCount = 0
    private var writeCount = 0

    fun trackRead(path: String, from: String = "") {
        readCount++
        Log.d(
            "FirestoreTrack",
            "Read from UserRepo [$readCount]: $path${if (from.isNotBlank()) " ($from)" else ""}"
        )
    }

    fun trackWrite(path: String, from: String = "") {
        writeCount++
        Log.d(
            "FirestoreTrack",
            "Write from UserRepo [$writeCount]: $path${if (from.isNotBlank()) " ($from)" else ""}"
        )
    }

    fun logFirestoreUsage(tag: String = "FirestoreUsage") {
        Log.d(tag, "📊 Total Firestore Reads: $readCount")
        Log.d(tag, "📦 Total Firestore Writes: $writeCount")
    }

    fun getUserWithFriends(onComplete: (User, List<PublicUser>, List<PublicUser>, List<PublicUser>) -> Unit, onFailure: (Exception) -> Unit) {

        trackRead("functions/getUserWithFriends", "getUserWithFriends")

        Firebase.functions("europe-west3")
            .getHttpsCallable("getUserWithFriends")
            .call()
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *> ?: throw Exception("Malformed response")

                val userMap = data["user"] as? Map<*, *> ?: throw Exception("Missing user")
                val friendsList = data["friends"] as? List<*> ?: emptyList<Any>()
                val incomingList = data["incomingRequests"] as? List<*> ?: emptyList<Any>()
                val outgoingList = data["outgoingRequests"] as? List<*> ?: emptyList<Any>()

                val user = parseUser(userMap)
                val friends = friendsList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val incoming = incomingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val outgoing = outgoingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }

                onComplete(user, friends, incoming, outgoing)
            }
            .addOnFailureListener(onFailure)
    }

    private fun parseUser(map: Map<*, *>): User {
        return User(
            uid = map["uid"] as? String ?: "",
            username = map["username"] as? String ?: "",
            email = map["email"] as? String ?: "",
            moodStatus = map["moodStatus"] as? String ?: "",
            points = (map["points"] as? Number)?.toInt() ?: 0,
            isPro = map["isPro"] as? Boolean ?: false,
            blockList = map["blockList"] as? List<String> ?: emptyList(),
            reportCount = (map["reportCount"] as? Number)?.toInt() ?: 0,
            avatarId = (map["avatarId"] as? Number)?.toInt() ?: 0,
            friendList = map["friendList"] as? List<String> ?: emptyList(),
            incomingFriendRequests = map["incomingFriendRequests"] as? List<String> ?: emptyList(),
            outgoingFriendRequests = map["outgoingFriendRequests"] as? List<String> ?: emptyList()
        )
    }

    private fun parsePublicUser(map: Map<*, *>?): PublicUser? {
        if (map == null) return null
        return PublicUser(
            uid = map["uid"] as? String ?: return null,
            username = map["username"] as? String ?: "",
            moodStatus = map["moodStatus"] as? String ?: "",
            avatarId = (map["avatarId"] as? Number)?.toInt() ?: 0,
            points = (map["points"] as? Number)?.toInt() ?: 0
        )
    }


    fun createUserProfile(uid: String, email: String, username: String): Task<Void> {
        trackWrite("users_public/$uid (new user)")
        trackWrite("users_private/$uid (new user)")
        trackWrite("swipes/$uid (new user)") // ✅ New tracking

        val publicUser = PublicUser(
            uid = uid,
            username = username,
            moodStatus = "",
            avatarId = 0,
            points = 0
        )

        val privateUser = PrivateUser(
            uid = uid,
            email = email.lowercase(),
            blockList = emptyList(),
            reportCount = 0,
            isPro = false,
            friendList = emptyList(),
            incomingFriendRequests = emptyList(),
            outgoingFriendRequests = emptyList()
        )

        val swipeStatus = UserSwipeStatus(
            uid = uid,
            swipesUsed = 0,
            swipesGranted = 25
        )

        val publicRef = db.collection("users_public").document(uid)
        val privateRef = db.collection("users_private").document(uid)
        val swipeRef = db.collection("swipes").document(uid)

        val batch = db.batch()
        batch.set(publicRef, publicUser)
        batch.set(privateRef, privateUser)
        batch.set(swipeRef, swipeStatus) // ✅ Add swipe status to batch

        return batch.commit()
    }

    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        trackRead("users_public/$uid", "getPublicUser")
        return db.collection("users_public").document(uid).get()
    }

    fun getPrivateUser(uid: String): Task<DocumentSnapshot> {
        trackRead("users_private/$uid", "getPrivateUser")
        return db.collection("users_private").document(uid).get()
    }

    fun isUsernameTaken(username: String, onResult: (Boolean) -> Unit) {
        trackRead("users?username=$username", "isUsernameTaken")
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
        trackWrite("users_public/${user.uid}", "createUser")
        return db.collection("users_public").document(user.uid).set(user)
    }

    fun updateMood(uid: String, mood: String): Task<Void> {
        trackWrite("users_public/$uid → moodStatus", "updateMood")
        return db.collection("users_public").document(uid).update("moodStatus", mood)
    }

    fun blockUser(currentUid: String, blockedUid: String): Task<Void> {
        trackWrite("users_private/$currentUid → blockList", "blockUser")
        return db.collection("users_private").document(currentUid)
            .update("blockList", FieldValue.arrayUnion(blockedUid))
    }

    fun findUserByEmail(email: String, onResult: (String?) -> Unit) {
        trackRead("functions/getUidByEmail", "findUserByEmail")

        val data = mapOf("email" to email)

        Firebase.functions("europe-west3")
            .getHttpsCallable("getUidByEmail")
            .call(data)
            .addOnSuccessListener { result ->
                val uid = (result.data as? Map<*, *>)?.get("uid") as? String
                Log.d("EmailTracker", "Cloud function success: $uid")
                onResult(uid)
            }
            .addOnFailureListener { error ->
                Log.e("EmailTracker", "Cloud function failure: ${error.message}", error)
                onResult(null)
            }
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
            chunk.forEach { trackRead("users_public/$it", "getPublicUsers") }
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

    fun deleteFriend(currentUid: String, targetUid: String): Task<Void> {
        trackWrite("users_private/$currentUid → friendList (remove)")
        trackWrite("users_private/$targetUid → friendList (remove)")

        val currentRef = db.collection("users_private").document(currentUid)
        val targetRef = db.collection("users_private").document(targetUid)

        val batch = db.batch()
        batch.update(currentRef, "friendList", FieldValue.arrayRemove(targetUid))
        batch.update(targetRef, "friendList", FieldValue.arrayRemove(currentUid))

        return batch.commit()
    }

    suspend fun removeLocalFriend(uid: String) {
        friendDao.deleteByUid(uid)
    }

    fun getRandomUsersFromCloud(
        excludedIds: List<String>,
        onResult: (List<PublicUser>) -> Unit
    ) {
        val function = Firebase.functions("europe-west3") // ✅ Add this
            .getHttpsCallable("getRandomEligibleUsers")
        val data = mapOf("excludedIds" to excludedIds)

        Log.d("RandomUser", "📤 Calling cloud function with excludedIds=$excludedIds")

        function.call(data)
            .addOnSuccessListener { result ->
                val usersList = result.data as? List<*> ?: run {
                    Log.w("RandomUser", "⚠️ Cloud function returned null or wrong type")
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                Log.d("RandomUser", "✅ Received ${usersList.size} user(s) from cloud")

                val publicUsers = usersList.mapNotNull { item ->
                    item as? Map<*, *> ?: return@mapNotNull null

                    val uid = item["uid"] as? String
                    val username = item["username"] as? String
                    val moodStatus = item["moodStatus"] as? String
                    val avatarId = (item["avatarId"] as? Number)?.toInt()
                    val points = (item["points"] as? Number)?.toInt()

                    if (uid == null) {
                        Log.w("RandomUser", "⚠️ Skipping user with missing uid: $item")
                        return@mapNotNull null
                    }

                    Log.d("RandomUser", "→ Parsed user: $uid ($username)")

                    PublicUser(
                        uid = uid,
                        username = username ?: "",
                        moodStatus = moodStatus ?: "",
                        avatarId = avatarId ?: 0,
                        points = points ?: 0
                    )
                }

                onResult(publicUsers)
            }
            .addOnFailureListener { error ->
                Log.e("RandomUser", "❌ Cloud function call failed: ${error.message}", error)
                onResult(emptyList())
            }
    }

    // Add more: reportUser(), updatePoints(), etc.

    fun getSwipeStatus(uid: String, onComplete: (UserSwipeStatus?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val local = swipeDao.getSwipeStatus(uid)
            if (local != null) {
                Log.d("SwipeStatus", "📦 Loaded from Room: $local")
                onComplete(
                    UserSwipeStatus(
                        uid = local.uid,
                        swipesUsed = local.swipesUsed,
                        swipesGranted = local.swipesGranted
                    )
                )
            } else {
                // fallback to Firestore
                db.collection("swipes").document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val status = doc.toObject(UserSwipeStatus::class.java)
                        if (status != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                swipeDao.insertSwipeStatus(
                                    LocalSwipeStatus(status.uid, status.swipesUsed, status.swipesGranted)
                                )
                            }
                        }
                        onComplete(status)
                    }
                    .addOnFailureListener {
                        Log.e("SwipeStatus", "❌ Firestore failed", it)
                        onComplete(null)
                    }
            }
        }
    }

    fun incrementSwipeCount(uid: String): Task<Void> {
        val swipeRef = db.collection("swipes").document(uid)

        return swipeRef.update("swipesUsed", FieldValue.increment(1))
            .addOnSuccessListener {
                // ✅ Fetch updated swipe count from Firestore and update Room
                db.collection("swipes").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val updated = doc.toObject(UserSwipeStatus::class.java)
                        if (updated != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                swipeDao.insertSwipeStatus(
                                    LocalSwipeStatus(
                                        uid = updated.uid,
                                        swipesUsed = updated.swipesUsed,
                                        swipesGranted = updated.swipesGranted
                                    )
                                )
                                Log.d("SwipeSync", "✅ Local swipes updated after increment")
                            }
                        }
                    }
            }
    }


    /////////////ROOM Database///////////////


    suspend fun syncFriendsToLocal(uids: List<String>, publicFriends: List<PublicUser>) {
        val newLocalFriends = publicFriends.map {
            LocalFriend(
                uid = it.uid,
                username = it.username,
                moodStatus = it.moodStatus,
                avatarId = it.avatarId,
                points = it.points
            )
        }.sortedBy { it.uid }

        val existingFriends = friendDao.getAllFriendsNow().sortedBy { it.uid }

        if (existingFriends != newLocalFriends) {
            Log.d("SyncFriends", "🔄 Local Room DB differs from Cloud — syncing")
            friendDao.clearFriends()
            friendDao.insertAll(newLocalFriends)
        } else {
            Log.d("SyncFriends", "✅ No changes detected — skipping sync")
        }
    }

    suspend fun getPublicUsersSuspend(uids: List<String>): List<PublicUser> =
        suspendCoroutine { cont ->
            getPublicUsers(uids) { users -> cont.resume(users) }
        }


    suspend fun getLocalFriend(uid: String): LocalFriend? {
        return friendDao.getFriendByUid(uid)
    }

    suspend fun hardResetFriends() {
        friendDao.clearFriends()
    }


    suspend fun hardResetLocalMessages() {
        messageDao.clearLocalMessages()
    }

}


//getRandomEligibleUsers
//getRandomEligibleUsers
