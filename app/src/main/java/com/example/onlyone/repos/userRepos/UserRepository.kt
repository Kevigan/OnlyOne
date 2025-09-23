package com.example.onlyone.repos.userRepos

import android.util.Log
import com.example.onlyone.BuildConfig
import com.example.onlyone.data.FavouriteMessage
import com.example.onlyone.data.LocalFavoriteMessage
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
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregating repository that exposes a stable API to the ViewModel layer,
 * delegating to feature-specific repos and Cloud Functions.
 *
 * Keep this layer thin and synchronous in shape (suspend/Result/callbacks)
 * so UI code stays simple.
 */
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

    // -------- UserPublicRepo --------

    suspend fun updatePublicProfileSecure(
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = publicRepo.updateUserPublicProfileSecure(updates, onSuccess, onFailure)

    fun getPublicUser(uid: String) = publicRepo.getPublicUser(uid)

    /** Overload that sends a fully materialized favourite (no extra read by messageId). */
    suspend fun setFavouriteMessage(
        fav: LocalFavoriteMessage,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = publicRepo.setFavouriteMessage(
        messageId = fav.id,
        text = fav.content,
        fromUid = fav.senderId,
        senderUsername = fav.senderUsername,
        senderMood = fav.senderMood,
        onSuccess = onSuccess,
        onFailure = onFailure
    )

    suspend fun clearFavouriteMessage(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = publicRepo.clearFavouriteMessage(onSuccess, onFailure)

    // -------- UserPrivateRepo --------

    fun syncFcmToken() = private.syncFcmToken()

    fun updateNotificationSetting(
        uid: String,
        key: String,
        enabled: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = private.updateNotificationSetting(uid, key, enabled, onSuccess, onFailure)

    // -------- UserEngagementRepo --------

    fun fetchEngagementStatus(uid: String, onComplete: (UserEngagementStatus?) -> Unit) =
        engagement.fetchEngagementStatus(uid, onComplete)

    suspend fun incrementSwipeCount(): Boolean = engagement.incrementSwipeCount()

    // -------- UserUpgradeRepo --------

    fun upgradeFeature(
        feature: String,
        levels: Int,
        onSuccess: (Int, Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) = upgrade.upgradeFeature(feature, levels, onSuccess, onFailure)

    // -------- UserInventoryRepo --------

    suspend fun fetchInventory(uid: String): UserInventory? = inventory.fetchInventory(uid)
    suspend fun buyAvatar(avatarId: Int): UserInventory? = inventory.buyAvatar(avatarId)
    suspend fun buyMood(moodId: Int): UserInventory? = inventory.buyMood(moodId)
    suspend fun buyTheme(themeId: Int): UserInventory? = inventory.buyTheme(themeId)

    // -------- UserSettingsRepo --------

    suspend fun saveAppLanguage(uid: String, language: String) = settings.saveAppLanguage(uid, language)
    suspend fun getAppLanguage(uid: String): String = settings.getAppLanguage(uid)
    suspend fun saveSearchUserLanguage(uid: String, lang: String) = settings.saveSearchUserLanguage(uid, lang)
    suspend fun getSearchUserLanguage(uid: String): String = settings.getSearchUserLanguage(uid)
    suspend fun getLocalNotificationSettings(uid: String): Pair<Boolean, Boolean> = settings.getLocalNotificationSettings(uid)
    suspend fun saveLocalNotificationSettings(uid: String, msg: Boolean, feedback: Boolean) =
        settings.saveLocalNotificationSettings(uid, msg, feedback)

    // -------- UserFriendRepo --------

    fun sendFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.sendFriendRequest(fromUid, toUid, onComplete)

    fun cancelOutgoingFriendRequest(fromUid: String, toUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.cancelOutgoingFriendRequest(fromUid, toUid, onComplete)

    fun acceptFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.acceptFriendRequest(currentUid, requesterUid, onComplete)

    fun declineFriendRequest(currentUid: String, requesterUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.declineFriendRequest(currentUid, requesterUid, onComplete)

    fun deleteFriend(currentUid: String, targetUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.deleteFriend(currentUid, targetUid, onComplete)

    fun blockAndUnfriendUser(currentUid: String, blockedUid: String, onComplete: (Boolean, String?) -> Unit) =
        friendRepo.blockAndUnfriendUser(currentUid, blockedUid, onComplete)

    fun unblockUser(targetUid: String, onComplete: (Boolean) -> Unit) =
        friendRepo.unblockUser(targetUid, onComplete)

    fun findUserByEmail(email: String, onResult: (String?) -> Unit) =
        friendRepo.findUserByEmail(email, onResult)

    // -------- UserDiscoveryRepo --------

    fun observeWrittenToday(): Flow<List<WrittenTodayEntity>> = discoveryRepo.observeWrittenToday()
    suspend fun loadRandomUserBatchSuspend(excludedIds: List<String>, chatLanguage: String): List<PublicUser> =
        discoveryRepo.loadRandomUserBatchSuspend(excludedIds, chatLanguage)

    suspend fun getLocalFriend(uid: String): LocalFriend? = discoveryRepo.getLocalFriend(uid)
    suspend fun removeLocalFriend(uid: String) = discoveryRepo.removeLocalFriend(uid)
    suspend fun syncFriendsToLocal(uids: List<String>, publicFriends: List<PublicUser>) =
        discoveryRepo.syncFriendsToLocal(uids, publicFriends)

    suspend fun hardResetFriends() = discoveryRepo.hardResetFriends()
    suspend fun hardResetLocalMessages() = discoveryRepo.hardResetLocalMessages()

    // -------- UserAchievementRepo --------

    suspend fun getUserAchievements(): Map<String, Any>? = achievementRepo.getUserAchievements()
    suspend fun fetchAchievementDefinitions(): List<Map<String, Any>> = achievementRepo.fetchAchievementDefinitions()
    suspend fun fetchUserStats(): Map<String, Any>? = achievementRepo.fetchUserStats()

    // -------- Session bootstrap (CF: getUserWithFriends) --------

    /**
     * Fetch user + social lists + engagement in one call.
     * Uses Cloud Function `getUserWithFriends` (region: europe-west3).
     */
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
        Firebase.functions("europe-west3")
            .getHttpsCallable("getUserWithFriends")
            .call()
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *> ?: throw Exception("Malformed response")

                val userMap = data["user"] as? Map<*, *> ?: throw Exception("Missing user")

                // notifications map (String -> Boolean)
                val notificationsRaw = userMap["notifications"] as? Map<*, *> ?: emptyMap<Any, Any>()
                val notifications = notificationsRaw.mapNotNull { (k, v) ->
                    (k as? String)?.let { key -> key to (v as? Boolean ?: true) }
                }.toMap()

                // FavouriteMessage (robust Timestamp handling)
                val favouriteMessage = (userMap["favouriteMessage"] as? Map<*, *>)?.let { fm ->
                    val chosenAtAny = fm["chosenAt"]
                    val chosenAtTs = when (chosenAtAny) {
                        is Timestamp -> chosenAtAny
                        is Map<*, *> -> {
                            val seconds = (chosenAtAny["seconds"] ?: chosenAtAny["_seconds"]) as? Number
                            val nanos = (chosenAtAny["nanoseconds"] ?: chosenAtAny["_nanoseconds"]) as? Number
                            if (seconds != null && nanos != null)
                                Timestamp(seconds.toLong(), nanos.toInt())
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

                // Adult flag (back-compat with older payloads)
                val isAdultFlag: Boolean = when (val v = userMap["isAdult"]) {
                    is Boolean -> v
                    else -> ((userMap["age"] as? Number)?.toInt() ?: 18) >= 18
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

                    ownedAvatars = (userMap["ownedAvatars"] as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList(),
                    ownedMoods   = (userMap["ownedMoods"]   as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList(),
                    ownedThemes  = (userMap["ownedThemes"]  as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList(),

                    blockList = (userMap["blockList"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    friendList = (userMap["friendList"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    incomingFriendRequests = (userMap["incomingFriendRequests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    outgoingFriendRequests = (userMap["outgoingFriendRequests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),

                    maxMessageLength = (userMap["maxMessageLength"] as? Number)?.toInt() ?: 25,
                    maxMoments       = (userMap["maxMoments"]       as? Number)?.toInt() ?: 75,
                    maxSwipes        = (userMap["maxSwipes"]        as? Number)?.toInt() ?: 50,
                    maxAdsPerDay     = (userMap["maxAdsPerDay"]     as? Number)?.toInt() ?: 3,
                    maxMoodLength    = (userMap["maxMoodLength"]    as? Number)?.toInt() ?: 25,

                    notifications = notifications,
                    reportCount = (userMap["reportCount"] as? Number)?.toInt() ?: 0,

                    achievementCount = (userMap["achievementCount"] as? Number)?.toInt() ?: 0,
                    favouriteMessage = favouriteMessage,

                    // public extras
                    gender = (userMap["gender"] as? String) ?: "unspecified",
                    age = (userMap["age"] as? Number)?.toInt(),
                    city = (userMap["city"] as? String) ?: "",

                    // private extras
                    isVerified = userMap["isVerified"] as? Boolean ?: false,
                    isAdult = isAdultFlag
                )

                val engagementMap = data["engagementStatus"] as? Map<*, *> ?: emptyMap<String, Any>()
                val engagementStatus = UserEngagementStatus(
                    uid = engagementMap["uid"] as? String ?: "",
                    swipesUsed = (engagementMap["swipesUsed"] as? Number)?.toInt() ?: 0,
                    momentsAvailable = (engagementMap["momentsAvailable"] as? Number)?.toInt() ?: 0,
                    adsWatchedToday = (engagementMap["adsWatchedToday"] as? Number)?.toInt() ?: 0,
                    lastRefill = engagementMap["lastRefill"] as? Timestamp
                )

                val friends = (data["friends"] as? List<*>)?.mapNotNull { parsePublicUser(it as? Map<*, *>) } ?: emptyList()
                val incoming = (data["incomingRequests"] as? List<*>)?.mapNotNull { parsePublicUser(it as? Map<*, *>) } ?: emptyList()
                val outgoing = (data["outgoingRequests"] as? List<*>)?.mapNotNull { parsePublicUser(it as? Map<*, *>) } ?: emptyList()
                val blocked = (data["blockedUsers"] as? List<*>)?.mapNotNull { parsePublicUser(it as? Map<*, *>) } ?: emptyList()

                Log.d("userStuff", "🔥 user name: ${user.username}")
                Log.d("ownedAvatars", "🔥 user ownedAvatars: ${user.ownedAvatars}")
                Log.d("userStuff", "🔥 user gold: ${user.gold}")
                Log.d("userStuff", "🔥 user maxMoments: ${user.maxMoments}")
                Log.d("userStuff", "🔥 engagementStatus momentsAvailable: ${engagementStatus.momentsAvailable}")

                onComplete(user, friends, incoming, outgoing, blocked, engagementStatus)
            }
            .addOnFailureListener(onFailure)
    }

    // -------- Parsing helpers --------

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
            favouriteMessage = favourite,
            gender = (map["gender"] as? String ?: "unspecified"),
            age = (map["age"] as? Number)?.toInt(),
            city = (map["city"] as? String ?: "")
        )
    }

    // -------- Account creation --------

    /**
     * Creates profile via CF `createUserProfile`.
     * Applies lightweight client-side validation to mirror server rules.
     */
    fun createUserProfile(
        email: String,
        username: String,
        fcmToken: String?,
        chatLanguage: String = "any",
        gender: String = "unspecified",   // accepts "m"|"f"|"d" too (mapped below)
        age: Int,                         // required 18..100
        city: String = "",
        ageAffirmation: Boolean,          // must be true
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val normalizedGender = when (gender.lowercase()) {
            "m", "male" -> "male"
            "f", "female" -> "female"
            "d", "nb", "nonbinary", "diverse" -> "nonbinary"
            "other" -> "other"
            else -> "unspecified"
        }

        if (age < 18 || age > 100) {
            onFailure(IllegalArgumentException("Age must be between 18 and 100."))
            return
        }
        if (!ageAffirmation) {
            onFailure(IllegalStateException("You must confirm you are 18+."))
            return
        }

        val data = hashMapOf(
            "email" to email,
            "username" to username,
            "fcmToken" to (fcmToken ?: ""),
            "chatLanguage" to chatLanguage.lowercase(),
            "gender" to normalizedGender,
            "city" to city.trim(),
            "age" to age,
            "ageAffirmation" to true
        )

        Firebase.functions("europe-west3")
            .getHttpsCallable("createUserProfile")
            .call(data)
            .addOnSuccessListener { result ->
                val success = (result.data as? Map<*, *>)?.get("success") as? Boolean ?: false
                if (success) onSuccess() else onFailure(Exception("Cloud Function returned success = false"))
            }
            .addOnFailureListener(onFailure)
    }

    // -------- Achievements seeding (admin/dev) --------

    /**
     * Seeds definitions using CF `seedAchievementDefinitions`.
     * Properly invokes the provided callbacks with the created/updated count.
     */
    fun seedAchievementDefinitions(
        definitions: List<Map<String, Any>>,
        onSuccess: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Firebase.functions("europe-west3")
            .getHttpsCallable("seedAchievementDefinitions")
            .call(mapOf("definitions" to definitions))
            .addOnSuccessListener { res ->
                val count = (res.data as? Map<*, *>)?.get("count") as? Int ?: 0
                Log.d("Seeder", "✅ Seeded $count definitions.")
                onSuccess(count)
            }
            .addOnFailureListener { e ->
                Log.e("Seeder", "❌ Failed to seed: ${e.message}", e)
                onFailure(e)
            }
    }

    // -------- Feedback --------

    sealed class FeedbackResult {
        object Success : FeedbackResult()
        object AlreadySubmitted : FeedbackResult() // one per day hit
        data class Error(val message: String) : FeedbackResult()
    }

    /**
     * Sends daily user feedback via CF `sendUserFeedback`.
     * Returns AlreadySubmitted if the daily limit is hit.
     */
    suspend fun sendUserFeedback(
        answers: Map<String, String>,
        text: String = "",
        platform: String = "android",
        appVersion: String = BuildConfig.VERSION_NAME,
        lang: String = "en"
    ): FeedbackResult = try {
        val payload = mapOf(
            "answers" to answers,
            "text" to text,
            "client" to mapOf(
                "platform" to platform,
                "appVersion" to appVersion,
                "lang" to lang
            )
        )
        Firebase.functions("europe-west3")
            .getHttpsCallable("sendUserFeedback")
            .call(payload)
            .await()
        FeedbackResult.Success
    } catch (e: Exception) {
        val fe = e as? FirebaseFunctionsException
        when (fe?.code) {
            FirebaseFunctionsException.Code.ALREADY_EXISTS -> FeedbackResult.AlreadySubmitted
            else -> FeedbackResult.Error(fe?.message ?: e.message ?: "Failed to send feedback.")
        }
    }

    // -------- Ads / swipe reset --------

    data class AdResetResult(
        val swipesUsed: Int,
        val maxSwipes: Int,
        val adsUsed: Int
    )

    /**
     * Marks an ad watched and returns updated swipe/ads counters.
     */
    suspend fun watchAdResetSwipes(): Result<AdResetResult> = try {
        val res = Firebase.functions("europe-west3")
            .getHttpsCallable("watchAdResetSwipes")
            .call()
            .await()
            .data as Map<*, *>

        val swipesUsed = (res["swipesUsed"] as? Number)?.toInt() ?: 0
        val maxSwipes  = (res["maxSwipes"]  as? Number)?.toInt() ?: 25
        val adsUsed    = (res["adsUsed"]    as? Number)?.toInt() ?: 0

        Result.success(AdResetResult(swipesUsed, maxSwipes, adsUsed))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
