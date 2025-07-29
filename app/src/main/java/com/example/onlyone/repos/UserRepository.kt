package com.example.onlyone.repos

import android.util.Log
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.SwipeDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.LocalSwipeStatus
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.User
import com.example.onlyone.data.UserSwipeStatus
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val friendDao: FriendDao,
    private val messageDao: MessageDao,
    private val swipeDao: SwipeDao,
    private val auth: FirebaseAuth
) {
    private val publicUserCache = mutableMapOf<String, PublicUser>()

    private var readCount = 0
    private var writeCount = 0
    fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "❌ Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUid = auth.currentUser?.uid

            if (currentUid != null && token != null) {
                db.collection("users_private")
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

    fun getUserWithFriends(
        onComplete: (
            User,
            List<PublicUser>, // friends
            List<PublicUser>, // incoming
            List<PublicUser>, // outgoing
            List<PublicUser>  // blocked
        ) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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
                val blockedList = data["blockedUsers"] as? List<*> ?: emptyList<Any>() // ✅ new line

                val user = parseUser(userMap)
                val friends = friendsList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val incoming = incomingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val outgoing = outgoingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val blocked = blockedList.mapNotNull { parsePublicUser(it as? Map<*, *>) } // ✅ new line
                Log.d("UserPoints", "🔥 user points: ${user.points}")

                onComplete(user, friends, incoming, outgoing, blocked)
            }
            .addOnFailureListener(onFailure)
    }

    private fun parseUser(map: Map<*, *>): User {
        val vpoints = (map["points"] as? Number)?.toInt() ?: 0
        Log.d("UserPoints", "🔥 user points from map = $vpoints")

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
            outgoingFriendRequests = map["outgoingFriendRequests"] as? List<String> ?: emptyList(),
            maxMessageLength = (map["maxMessageLength"] as? Number)?.toInt() ?: 25, // ✅
            gold = (map["gold"] as? Number)?.toInt() ?: 0,
            runes_rare = (map["runes_rare"] as? Number)?.toInt() ?: 0,
            runes_super_rare = (map["runes_super_rare"] as? Number)?.toInt() ?: 0,
            runes_mega_rare = (map["runes_mega_rare"] as? Number)?.toInt() ?: 0,
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

    fun createUserProfile(
        email: String,
        username: String,
        fcmToken: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "email" to email,
            "username" to username,
            "fcmToken" to (fcmToken ?: "")
        )

        Firebase.functions("europe-west3")
            .getHttpsCallable("createUserProfile")
            .call(data)
            .addOnSuccessListener { result ->
                val success = (result.data as? Map<*, *>)?.get("success") as? Boolean ?: false
                if (success) {
                    onSuccess()
                } else {
                    onFailure(Exception("Cloud Function returned success = false"))
                }
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    fun getPublicUser(uid: String): Task<DocumentSnapshot> {
        trackRead("users_public/$uid", "getPublicUser")
        return db.collection("users_public").document(uid).get()
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

    fun updateMood(uid: String, mood: String): Task<Void> {
        trackWrite("users_public/$uid → moodStatus", "updateMood")
        return db.collection("users_public").document(uid).update("moodStatus", mood)
    }

    fun blockAndUnfriendUser(
        currentUid: String,
        blockedUid: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val data = mapOf("blockedUid" to blockedUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("blockAndUnfriendUser")
            .call(data)
            .addOnSuccessListener { result ->
                Log.d("BlockUser", "✅ Blocked (and unfriended if needed) via cloud")

                // optional: check return value if you want to inspect result.data
                val response = result.data as? Map<*, *>
                val unfriended = response?.get("unfriended") as? Boolean ?: false
                Log.d("BlockUser", "→ Was unfriended: $unfriended")

                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("BlockUser", "❌ Failed to block user: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun unblockUser(targetUid: String, onComplete: (Boolean) -> Unit) {
        val data = mapOf("targetUid" to targetUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("unblockUser")
            .call(data)
            .addOnSuccessListener {
                Log.d("UnblockUser", "✅ Unblocked user $targetUid")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("UnblockUser", "❌ Failed to unblock user: ${e.message}", e)
                onComplete(false)
            }
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

    fun sendFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("toUid" to toUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("sendFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendRequest", "✅ Friend request sent via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendRequest", "❌ Failed: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun cancelOutgoingFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("toUid" to toUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("cancelOutgoingFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendCancel", "✅ Outgoing request canceled via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendCancel", "❌ Failed to cancel outgoing request: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun acceptFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("requesterUid" to requesterUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("acceptFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendAccept", "✅ Friend accepted via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendAccept", "❌ Failed to accept friend: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun declineFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("requesterUid" to requesterUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("declineFriendRequest")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendDecline", "✅ Declined request via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendDecline", "❌ Failed to decline request: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun deleteFriend(currentUid: String, targetUid: String, onComplete: (Boolean, String?) -> Unit) {
        val data = mapOf("targetUid" to targetUid)

        Firebase.functions("europe-west3")
            .getHttpsCallable("deleteFriend")
            .call(data)
            .addOnSuccessListener {
                Log.d("FriendDelete", "✅ Friend deleted via cloud")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FriendDelete", "❌ Failed to delete friend: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun upgradeMaxMessageLength(
        levels: Int,
        onSuccess: (Int, Int) -> Unit, // newLength, remainingPoints
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf("levels" to levels)

        Firebase.functions("europe-west3")
            .getHttpsCallable("upgradeMessageLength")
            .call(data)
            .addOnSuccessListener { result ->
                val dataMap = result.data as? Map<*, *> ?: return@addOnSuccessListener
                val newLength = (dataMap["newLength"] as? Number)?.toInt() ?: 0
                val remainingGold = (dataMap["remainingGold"] as? Number)?.toInt() ?: 0
                onSuccess(newLength, remainingGold)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
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

    fun incrementSwipeCount(onComplete: (Boolean) -> Unit) {
        Firebase.functions("europe-west3")
            .getHttpsCallable("incrementSwipeCount")
            .call()
            .addOnSuccessListener {
                Log.d("SwipeIncrement", "✅ Cloud swipe increment success")

                CoroutineScope(Dispatchers.IO).launch {
                    val local = swipeDao.getSwipeStatus(Firebase.auth.currentUser!!.uid)
                    if (local != null) {
                        val updated = local.copy(swipesUsed = local.swipesUsed + 1)
                        swipeDao.insertSwipeStatus(updated)
                        Log.d("SwipeIncrement", "✅ Local swipes incremented to ${updated.swipesUsed}")
                    }
                    onComplete(true)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SwipeIncrement", "❌ Swipe increment failed: ${e.message}", e)
                onComplete(false)
            }
    }

    fun maybeResetSwipes(onComplete: (Boolean) -> Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return onComplete(false)

        Firebase.functions("europe-west3")
            .getHttpsCallable("resetDailySwipes")
            .call()
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *>
                val reset = data?.get("reset") as? Boolean ?: false

                Log.d("SwipeReset", if (reset) "✅ Cloud reset succeeded" else "ℹ️ Cloud check: no reset")

                // 🔄 Always refresh local copy
                updateLocalSwipeStatusFromFirestore(uid) {
                    onComplete(reset)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SwipeReset", "❌ Failed to call reset function", e)
                onComplete(false)
            }
    }

    fun syncSwipeStatusFromCloud(uid: String, onComplete: () -> Unit) {
        updateLocalSwipeStatusFromFirestore(uid, onComplete)
    }

    private fun updateLocalSwipeStatusFromFirestore(uid: String, onComplete: () -> Unit) {
        db.collection("swipes").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val status = doc.toObject(UserSwipeStatus::class.java)
                if (status != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        swipeDao.insertSwipeStatus(
                            LocalSwipeStatus(
                                uid = status.uid,
                                swipesUsed = status.swipesUsed,
                                swipesGranted = status.swipesGranted
                            )
                        )
                        Log.d("SwipeSync", "✅ Room updated with fresh swipe data")
                        onComplete()
                    }
                } else {
                    Log.w("SwipeSync", "⚠️ No swipe status found in Firestore")
                    onComplete()
                }
            }
            .addOnFailureListener {
                Log.e("SwipeSync", "❌ Firestore read failed", it)
                onComplete()
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