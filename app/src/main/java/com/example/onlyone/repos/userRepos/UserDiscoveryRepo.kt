package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.WrittenTodayEntity
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
        val function = Firebase.functions("europe-west3") // ✅ Add this
            .getHttpsCallable("getRandomEligibleUsers")
        val data = mapOf("excludedIds" to excludedIds,  "chatLanguage" to chatLanguage)

        Log.d("RandomUser", "📤 Calling cloud function with excludedIds=$excludedIds")

        function.call(data)
            .addOnSuccessListener { result ->
                val usersList = result.data
                if (usersList !is List<*>) {
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
                    val chatLanguage = item["chatLanguage"] as? String ?: "en"

                    if (uid == null) {
                        Log.w("RandomUser", "⚠️ Skipping user with missing uid: $item")
                        return@mapNotNull null
                    }

                    Log.d("RandomUser", "→ Parsed user: $uid ($username)")

                    PublicUser(
                        uid = uid,
                        username = username ?: "",
                        moodStatus = moodStatus ?: "",
                        chatLanguage = chatLanguage,
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

    suspend fun syncFriendsToLocal(uids: List<String>, publicFriends: List<PublicUser>) {
        val newLocalFriends = publicFriends.map { pu ->
            LocalFriend(
                uid = pu.uid,
                username = pu.username,
                moodStatus = pu.moodStatus,
                avatarId = pu.avatarId,
                points = pu.points,
                achievementCount = pu.achievementCount,     // no ?: 0 if non-null in model
                favouriteMessage = pu.favouriteMessage,

                // 🆕 passthroughs
                gender = pu.gender,
                age = pu.age,
                city = pu.city
            )
        }.sortedBy { it.uid }

        val existing = friendDao.getAllFriendsNow().sortedBy { it.uid }

        if (existing != newLocalFriends) {
            friendDao.clearFriends()
            friendDao.insertAll(newLocalFriends)
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
