package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.data.FavouriteMessage
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.data.UserInventory
import com.example.onlyone.data.WrittenTodayEntity
import com.example.onlyone.repos.UserEngagementRepo
import com.example.onlyone.repos.UserInventoryRepo
import com.example.onlyone.repos.UserSettingsRepo
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val publicRepo: UserPublicRepo,
    private val private: UserPrivateRepo,
    private val engagement: UserEngagementRepo,
    private val upgrade: UserUpgradeRepo,
    private val inventory: UserInventoryRepo,
    private val settings: UserSettingsRepo,
    private val friendRepo: UserFriendRepo,
    private val discoveryRepo: UserDiscoveryRepo,
    private val achievementRepo: UserAchievementRepo
) {
    //////////UserPublicRepo//////////
    suspend fun updatePublicProfileSecure(updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = publicRepo.updateUserPublicProfileSecure(updates, onSuccess, onFailure)
    fun getPublicUser(uid: String) = publicRepo.getPublicUser(uid)
    suspend fun setFavouriteMessage(messageId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = publicRepo.setFavouriteMessage(messageId, onSuccess, onFailure)
    suspend fun clearFavouriteMessage(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = publicRepo.clearFavouriteMessage(onSuccess, onFailure)
    //////////UserPublicRepo End//////////


    //////////UserPrivateRepo//////////
    fun syncFcmToken() = private.syncFcmToken()
    fun updateNotificationSetting(uid: String, key: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = private.updateNotificationSetting(uid, key, enabled, onSuccess, onFailure)
    //////////UserPrivateRepo End//////////


    //////////UserEngagementRepo//////////
    fun fetchEngagementStatus(uid: String, onComplete: (UserEngagementStatus?) -> Unit) = engagement.fetchEngagementStatus(uid, onComplete)
    suspend fun incrementSwipeCount(): Boolean = engagement.incrementSwipeCount()
    //////////UserEngagementRepo End//////////


    //////////UserUpgradeRepo//////////
    fun upgradeFeature(feature: String, levels: Int, onSuccess: (Int, Int) -> Unit, onFailure: (Exception) -> Unit) = upgrade.upgradeFeature(feature, levels, onSuccess, onFailure)
    //////////UserUpgradeRepo End//////////


    //////////UserInventoryRepo//////////
    suspend fun fetchInventory(uid: String): UserInventory? = inventory.fetchInventory(uid)
    suspend fun buyAvatar(avatarId: Int): UserInventory?= inventory.buyAvatar(avatarId)
    suspend fun buyMood(moodId: Int): UserInventory? = inventory.buyMood(moodId)
    //////////UserInventoryRepo End//////////


    //////////UserSettingsRepo//////////
    suspend fun saveAppLanguage(uid: String, language: String) = settings.saveAppLanguage(uid, language)
    suspend fun getAppLanguage(uid: String): String = settings.getAppLanguage(uid)
    suspend fun saveSearchUserLanguage(uid: String, lang: String) = settings.saveSearchUserLanguage(uid, lang)
    suspend fun getSearchUserLanguage(uid: String): String = settings.getSearchUserLanguage(uid)
    suspend fun getLocalNotificationSettings(uid: String): Pair<Boolean, Boolean> = settings.getLocalNotificationSettings(uid)
    suspend fun saveLocalNotificationSettings(uid: String, msg: Boolean, feedback: Boolean) = settings.saveLocalNotificationSettings(uid, msg, feedback)
    //////////UserSettingsRepo End//////////


    //////////UserFriendRepo//////////
    fun sendFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.sendFriendRequest(fromUid, toUid, onComplete) }
    fun cancelOutgoingFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.cancelOutgoingFriendRequest(fromUid, toUid, onComplete) }
    fun acceptFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.acceptFriendRequest(currentUid, requesterUid, onComplete) }
    fun declineFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.declineFriendRequest(currentUid, requesterUid, onComplete) }
    fun deleteFriend(currentUid: String, targetUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.deleteFriend(currentUid, targetUid, onComplete) }
    fun blockAndUnfriendUser(currentUid: String, blockedUid: String, onComplete: (Boolean, String?) -> Unit) { friendRepo.blockAndUnfriendUser(currentUid, blockedUid, onComplete) }
    fun unblockUser(targetUid: String, onComplete: (Boolean) -> Unit) { friendRepo.unblockUser(targetUid, onComplete) }
    fun findUserByEmail(email: String, onResult: (String?) -> Unit) { friendRepo.findUserByEmail(email, onResult) }
    //////////UserFriendRepo End//////////


    //////////UserDiscoveryRepo//////////
    fun observeWrittenToday(): Flow<List<WrittenTodayEntity>> { return discoveryRepo.observeWrittenToday() }
    suspend fun loadRandomUserBatchSuspend(excludedIds: List<String>, chatLanguage: String): List<PublicUser> { return discoveryRepo.loadRandomUserBatchSuspend(excludedIds, chatLanguage) }
    suspend fun getLocalFriend(uid: String): LocalFriend? { return discoveryRepo.getLocalFriend(uid) }
    suspend fun removeLocalFriend(uid: String){return discoveryRepo.removeLocalFriend(uid)}
    suspend fun syncFriendsToLocal(uids: List<String>, publicFriends: List<PublicUser>) { discoveryRepo.syncFriendsToLocal(uids, publicFriends) }
    suspend fun hardResetFriends() { discoveryRepo.hardResetFriends() }
    suspend fun hardResetLocalMessages() { discoveryRepo.hardResetLocalMessages() }
    //////////UserDiscoveryRepo End//////////


    //////////UserAchievemtRepo//////////
    suspend fun getUserAchievements(): Map<String, Any>? { return achievementRepo.getUserAchievements() }
    suspend fun fetchAchievementDefinitions(): List<Map<String, Any>> { return achievementRepo.fetchAchievementDefinitions() }
    suspend fun fetchUserStats(): Map<String, Any>? { return achievementRepo.fetchUserStats() }

    //////////UserAchievemtRepo End//////////





    fun fetchFullUserSession(
        onComplete: (
            UserComposite,
            List<PublicUser>, // friends
            List<PublicUser>, // incoming
            List<PublicUser>, // outgoing
            List<PublicUser>, // blocked
            UserEngagementStatus
        ) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        //trackRead("functions/getUserWithFriends", "getUserWithFriends")

        Firebase.functions("europe-west3")
            .getHttpsCallable("getUserWithFriends")
            .call()
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *> ?: throw Exception("Malformed response")

                val userMap = data["user"] as? Map<*, *> ?: throw Exception("Missing user")

                val notificationsRaw = userMap["notifications"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val notifications = notificationsRaw.mapNotNull { (k, v) ->
                    (k as? String)?.let { key -> key to (v as? Boolean ?: true) }
                }.toMap()

                // Build FavouriteMessage from the mergedUser map (robust Timestamp handling)
                val favouriteMessage = (userMap["favouriteMessage"] as? Map<*, *>)?.let { fm ->
                    val chosenAtAny = fm["chosenAt"]
                    val chosenAtTs = when (chosenAtAny) {
                        is com.google.firebase.Timestamp -> chosenAtAny
                        is Map<*, *> -> {
                            val seconds = (chosenAtAny["seconds"] ?: chosenAtAny["_seconds"]) as? Number
                            val nanos   = (chosenAtAny["nanoseconds"] ?: chosenAtAny["_nanoseconds"]) as? Number
                            if (seconds != null && nanos != null)
                                com.google.firebase.Timestamp(seconds.toLong(), nanos.toInt())
                            else null
                        }
                        else -> null
                    }

                    FavouriteMessage(
                        text = fm["text"] as? String ?: "",
                        fromUid = fm["fromUid"] as? String ?: "",
                        messageId = fm["messageId"] as? String ?: "",
                        chosenAt = chosenAtTs
                    )
                }

                val user = UserComposite(
                    uid = userMap["uid"] as? String ?: "",
                    email = userMap["email"] as? String ?: "",
                    username = userMap["username"] as? String ?: "",
                    avatarId = (userMap["avatarId"] as? Number)?.toInt() ?: 0,
                    moodId = (userMap["moodId"] as? Number)?.toInt() ?: 0,
                    moodStatus = userMap["moodStatus"] as? String ?: "",
                    chatLanguage = userMap["chatLanguage"] as? String ?: "en",
                    isPro = userMap["isPro"] as? Boolean ?: false,

                    gold = (userMap["gold"] as? Number)?.toInt() ?: 0,
                    points = (userMap["points"] as? Number)?.toInt() ?: 0,

                    runes_rare = (userMap["runes_rare"] as? Number)?.toInt() ?: 0,
                    runes_super_rare = (userMap["runes_super_rare"] as? Number)?.toInt() ?: 0,
                    runes_mega_rare = (userMap["runes_mega_rare"] as? Number)?.toInt() ?: 0,

                    blockList = userMap["blockList"] as? List<String> ?: emptyList(),
                    friendList = userMap["friendList"] as? List<String> ?: emptyList(),
                    incomingFriendRequests = userMap["incomingFriendRequests"] as? List<String> ?: emptyList(),
                    outgoingFriendRequests = userMap["outgoingFriendRequests"] as? List<String> ?: emptyList(),

                    maxMessageLength = (userMap["maxMessageLength"] as? Number)?.toInt() ?: 25,
                    maxMoments = (userMap["maxMoments"] as? Number)?.toInt() ?: 75,  // 🔧 match backend default
                    maxSwipes = (userMap["maxSwipes"] as? Number)?.toInt() ?: 50,
                    maxAdsPerDay = (userMap["maxAdsPerDay"] as? Number)?.toInt() ?: 3,
                    maxMoodLength = (userMap["maxMoodLength"] as? Number)?.toInt() ?: 25,

                    notifications = notifications,
                    reportCount = (userMap["reportCount"] as? Number)?.toInt() ?: 0,

                    ownedAvatars = (userMap["ownedAvatars"] as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList(),
                    ownedMoods = (userMap["ownedMoods"] as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList(),

                    // NEW fields from users_public we now return in the callable
                    achievementCount = (userMap["achievementCount"] as? Number)?.toInt() ?: 0,
                    favouriteMessage = favouriteMessage
                )


                val engagementMap = data["engagementStatus"] as? Map<*, *> ?: emptyMap<String, Any>()

                val engagementStatus = UserEngagementStatus(
                    uid = engagementMap["uid"] as? String ?: "",
                    swipesUsed = (engagementMap["swipesUsed"] as? Number)?.toInt() ?: 0,
                    momentsAvailable = (engagementMap["momentsAvailable"] as? Number)?.toInt() ?: 0,
                    adsWatchedToday = (engagementMap["adsWatchedToday"] as? Number)?.toInt() ?: 0,
                    lastRefill = engagementMap["lastRefill"] as? Timestamp
                )

                val friendsList = data["friends"] as? List<*> ?: emptyList<Any>()
                val incomingList = data["incomingRequests"] as? List<*> ?: emptyList<Any>()
                val outgoingList = data["outgoingRequests"] as? List<*> ?: emptyList<Any>()
                val blockedList = data["blockedUsers"] as? List<*> ?: emptyList<Any>()

                val friends = friendsList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val incoming = incomingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val outgoing = outgoingList.mapNotNull { parsePublicUser(it as? Map<*, *>) }
                val blocked = blockedList.mapNotNull { parsePublicUser(it as? Map<*, *>) }

                Log.d("userStuff", "🔥 user name: ${user.username}")
                Log.d("ownedAvatars", "🔥 user ownedAvatars: ${user.ownedAvatars}")
                Log.d("userStuff", "🔥 user gold: ${user.gold}")
                Log.d("userStuff", "🔥 user maxMoments: ${user.maxMoments}")
                Log.d("userStuff", "🔥 engagementStatus momentsAvailable: ${engagementStatus.momentsAvailable}")

                onComplete(user, friends, incoming, outgoing, blocked, engagementStatus)
            }
            .addOnFailureListener(onFailure)
    }

    private fun parseTimestamp(any: Any?): Timestamp? = when (any) {
        is Timestamp -> any
        is Map<*, *> -> {
            val seconds = (any["seconds"] ?: any["_seconds"]) as? Number
            val nanos = (any["nanoseconds"] ?: any["_nanoseconds"]) as? Number
            if (seconds != null && nanos != null) Timestamp(seconds.toLong(), nanos.toInt()) else null
        }
        else -> null
    }

    private fun parseFavourite(map: Map<*, *>?): FavouriteMessage? {
        if (map == null) return null
        return FavouriteMessage(
            text = map["text"] as? String ?: "",
            fromUid = map["fromUid"] as? String ?: "",
            messageId = map["messageId"] as? String ?: "",
            chosenAt = parseTimestamp(map["chosenAt"])
        )
    }

    private fun parsePublicUser(map: Map<*, *>?): PublicUser? {
        if (map == null) return null
        val uid = map["uid"] as? String ?: return null

        val favourite = parseFavourite(map["favouriteMessage"] as? Map<*, *>)

        return PublicUser(
            uid = uid,
            username = map["username"] as? String ?: "",
            moodStatus = map["moodStatus"] as? String ?: "",
            chatLanguage = map["chatLanguage"] as? String ?: "en",
            avatarId = (map["avatarId"] as? Number)?.toInt() ?: 0,
            moodId = (map["moodId"] as? Number)?.toInt() ?: 0,
            points = (map["points"] as? Number)?.toInt() ?: 0,
            achievementCount = (map["achievementCount"] as? Number)?.toInt() ?: 0,
            favouriteMessage = favourite
        )
    }

    fun createUserProfile(
        email: String,
        username: String,
        fcmToken: String?,
        chatLanguage: String = "any",
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "email" to email,
            "username" to username,
            "fcmToken" to (fcmToken ?: ""),
            "chatLanguage" to chatLanguage
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

    fun seedAchievementDefinitions(
        definitions: List<Map<String, Any>>,
        onSuccess: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = mapOf("definitions" to definitions)

        Firebase.functions("europe-west3")
            .getHttpsCallable("seedAchievementDefinitions")
            .call(mapOf("definitions" to definitions))
            .addOnSuccessListener {
                val count = (it.data as? Map<*, *>)?.get("count") as? Int ?: 0
                Log.d("Seeder", "✅ Seeded $count definitions.")
            }
            .addOnFailureListener {
                Log.e("Seeder", "❌ Failed to seed: ${it.message}", it)
            }

    }

}