package com.onlyone.app.repos.userRepos

import android.util.Log
import com.onlyone.app.dao.FriendDao
import com.onlyone.app.dao.MessageDao
import com.onlyone.app.data.FavouriteMessage
import com.onlyone.app.data.LocalFriend
import com.onlyone.app.data.PublicUser
import com.onlyone.app.data.WrittenTodayEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserDiscoveryRepo @Inject constructor(
    private val friendDao: FriendDao,
    private val messageDao: MessageDao,
    private val db: FirebaseFirestore
) {

    fun observeWrittenToday(): Flow<List<WrittenTodayEntity>> {
        return messageDao.observeWrittenToday()
    }

    suspend fun loadRandomUserBatchSuspend(
        excludedIds: List<String>,
        chatLanguage: String
    ): List<PublicUser> = suspendCoroutine { cont ->
        getRandomUsersFromCloud(excludedIds, chatLanguage) { users ->
            cont.resume(users)
        }
    }

    fun getRandomUsersFromCloud(
        excludedIds: List<String>,
        chatLanguage: String = "any",
        onResult: (List<PublicUser>) -> Unit
    ) {
        val function = Firebase.functions("europe-west3").getHttpsCallable("getRandomEligibleUsers")
        val data = mapOf("excludedIds" to excludedIds, "chatLanguage" to chatLanguage)

        Log.d("RandomUser", "📤 Calling CF getRandomEligibleUsers excludedIds=$excludedIds chatLanguage=$chatLanguage")

        function.call(data)
            .addOnSuccessListener { result ->
                val root = result.data
                // CF can return either a raw List or a Map with "users" -> List
                val usersList: List<*>? = when (root) {
                    is List<*> -> root
                    is Map<*, *> -> root["users"] as? List<*>
                    else -> null
                }

                if (usersList == null) {
                    Log.w("RandomUser", "⚠️ Unexpected CF payload shape: ${root?.javaClass?.name}")
                    onResult(emptyList()); return@addOnSuccessListener
                }

                Log.d("RandomUser", "✅ Received ${usersList.size} user(s) from cloud")

                val publicUsers = usersList.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null

                    // allow nested "profile" map as a fallback source
                    val profile = map["profile"] as? Map<*, *>

                    val uid        = map["uid"] as? String ?: return@mapNotNull null
                    val username   = map["username"] as? String ?: ""
                    val moodStatus = map["moodStatus"] as? String ?: ""
                    val lang       = (map["chatLanguage"] as? String) ?: "en"

                    val avatarId   = (map["avatarId"] as? Number)?.toInt() ?: 0
                    val moodId     = (map["moodId"] as? Number)?.toInt() ?: 0
                    val points     = (map["points"] as? Number)?.toInt() ?: 0
                    val achievementCount = (map["achievementCount"] as? Number)?.toInt() ?: 0

                    // preferred flat keys, fallback to nested profile.*
                    val gender     = (map["gender"] as? String)
                        ?: (profile?.get("gender") as? String)
                        ?: "unspecified"

                    val age: Int?  = when (val a = map["age"] ?: profile?.get("age")) {
                        is Number -> a.toInt()
                        is String -> a.toIntOrNull()
                        else -> null
                    }

                    val city       = (map["city"] as? String)
                        ?: (profile?.get("city") as? String)
                        ?: ""

                    val isAdult: Boolean = when (val v = map["isAdult"] ?: profile?.get("isAdult")) {
                        is Boolean -> v
                        is Number  -> v.toInt() != 0
                        is String  -> v.equals("true", true) || v == "1"
                        else       -> false
                    }

                    // FavouriteMessage? – keep this null-safe. Adjust mapping if your CF returns specific fields.
                    val favouriteMessage = (map["favouriteMessage"] as? Map<*, *>)?.let { favMap ->
                        favMap.toFavouriteMessageOrNull()
                    }

                    Log.d(
                        "RandomUser",
                        "→ Parsed user $uid ($username) age=$age gender=$gender city=$city isAdult=$isAdult"
                    )

                    PublicUser(
                        uid = uid,
                        username = username,
                        moodStatus = moodStatus,
                        chatLanguage = lang,
                        avatarId = avatarId,
                        moodId = moodId,
                        points = points,
                        achievementCount = achievementCount,
                        favouriteMessage = favouriteMessage,
                        gender = gender,
                        age = age,
                        city = city,
                        isAdult = isAdult
                    )
                }

                onResult(publicUsers)
            }
            .addOnFailureListener { error ->
                Log.e("RandomUser", "❌ CF call failed: ${error.message}", error)
                onResult(emptyList())
            }
    }

    suspend fun syncFriendsToLocal(
        serverFriendUids: List<String>,          // from user.friendList (authoritative list)
        publicFriends: List<PublicUser>          // only the friends you actually fetched (new/changed or full list)
    ) {
        // Current local snapshot
        val existing = friendDao.getAllFriendsNow()

        // 1) Compute removals (present locally but NOT on server list)
        val serverSet = serverFriendUids.toSet()
        val removedUids = existing.asSequence()
            .map { it.uid }
            .filter { it !in serverSet }
            .toList()

        // 2) Build upserts for any fetched friends (new/changed or full set)
        fun PublicUser.toLocal(): LocalFriend = LocalFriend(
            uid = uid,
            username = username,
            moodStatus = moodStatus,
            avatarId = avatarId,
            points = points,
            achievementCount = achievementCount,
            favouriteMessage = favouriteMessage,
            gender = gender,
            age = age,
            city = city,
            publicVersion = publicVersion           // 👈 keep the version!
        )
        val upserts = publicFriends.map { it.toLocal() }

        // 3) Apply delta in one transaction (no table wipe)
        if (removedUids.isNotEmpty() || upserts.isNotEmpty()) {
            friendDao.applyDelta(removedUids, upserts)
        }
    }


    suspend fun getLocalFriend(uid: String): LocalFriend? {
        return friendDao.getFriendByUid(uid)
    }

    suspend fun removeLocalFriend(uid: String) {
        friendDao.deleteByUid(uid)
    }

    suspend fun hardResetFriends() {
        friendDao.clearFriends()
    }

    suspend fun hardResetLocalMessages() {
        messageDao.clearLocalMessages()
    }
}
private fun Map<*, *>.toFavouriteMessageOrNull(): FavouriteMessage? = try {
    // TODO: map to your real FavouriteMessage fields.
    // Example shape (edit to match your data class):
    FavouriteMessage(
        // id       = this["id"] as? String ?: "",
        // text     = this["text"] as? String ?: "",
        // fromUid  = this["fromUid"] as? String ?: "",
        // timestamp= this["timestamp"] as? Timestamp ?: Timestamp.now()
    )
} catch (e: Exception) {
    null
}
