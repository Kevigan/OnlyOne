package com.onlyone.app.repos

import android.util.Log
import com.onlyone.app.dao.FavoriteMessageDao
import com.onlyone.app.dao.MessageDao
import com.onlyone.app.data.AdminReportItem
import com.onlyone.app.data.LocalFavoriteMessage
import com.onlyone.app.data.LocalMessage
import com.onlyone.app.data.Message
import com.onlyone.app.data.MessageResult
import com.onlyone.app.data.ReportReason
import com.onlyone.app.data.ReportResult
import com.onlyone.app.data.WrittenTodayEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val messageDao: MessageDao,
    private val favoriteMessageDao: FavoriteMessageDao
) {

    private var readCount = 0
    private var writeCount = 0

    private fun trackRead(path: String, from: String = "") {
        readCount++
        Log.d("ChatFirestore_Track", "[ChatRepo] Read #$readCount: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    private fun trackWrite(path: String, from: String = "") {
        writeCount++
        Log.d("ChatFirestore_Track", "[ChatRepo] Write #$writeCount: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    suspend fun sendMessage(message: Message): MessageResult {
        return try {
            val data = mapOf(
                "receiverId" to message.receiverId,
                "content" to message.content,
                "senderUsername" to message.senderUsername,
                "senderMood" to message.senderMood,
                "senderAvatarId" to message.senderAvatarId,
                "messageId" to message.id
            )

            val result = Firebase.functions("europe-west3")
                .getHttpsCallable("sendMessage")
                .call(data)
                .await()

            val response = result.data as? Map<*, *> ?: return MessageResult.Error

            val success = response["success"] as? Boolean ?: false
            val alreadySent = response["alreadySent"] as? Boolean ?: false

            when {
                alreadySent -> {
                    // 🔒 Server says you already sent today → reflect locally
                    recordWrittenUser(message.receiverId)
                    MessageResult.AlreadySent
                }
                success -> {
                    val rewards = response["rewards"] as? Map<*, *>
                    val gold = rewards?.get("gold") as? Int ?: 0
                    val points = rewards?.get("points") as? Int ?: 0
                    val rune = rewards?.get("runeEarned") as? String
                    Log.d("SendMessage", "✅ Rewards: $gold gold, $points points, rune: $rune")

                    // ✅ Mark as written locally immediately
                    recordWrittenUser(message.receiverId)

                    MessageResult.Success(gold, points, rune?.takeIf { it != "none" })
                }
                else -> MessageResult.Error
            }
        } catch (e: Exception) {
            Log.e("SendMessage", "❌ Cloud Function failed", e)
            MessageResult.Error
        }
    }

    suspend fun addFeedback(message: LocalMessage, feedback: Int) {
        if (message.feedback != -10) {
            Log.d("ChatRepo", "ℹ️ Feedback already set. Skipping update.")
            return
        }

        try {
            // 🔁 Await Firestore write
            db.collection("messages").document(message.id)
                .update("feedback", feedback)
                .await()

            // ✅ Then update Room
            val updated = message.copy(feedback = feedback)
            messageDao.insertAll(listOf(updated))
            trackWrite("messages/${message.id} → feedback:$feedback", "addFeedback")
        } catch (e: Exception) {
            Log.e("ChatRepo", "❌ Failed to update feedback", e)
        }
    }

    suspend fun markAsRead(message: LocalMessage) {
        if (!message.read) {
            if (message.id.isBlank()) {
                Log.e("ChatRepo", "❌ Invalid message ID (blank)")
                return
            }
            Log.d("MARK_READ", "markAsRead() called with id='${message.id}', content='${message.content}'")

            try {
                // Update Firestore
                db.collection("messages").document(message.id)
                    .update("read", true)
                    .await()

                // Update local Room DB
                val updated = message.copy(read = true)
                messageDao.insertAll(listOf(updated)) // replaces existing by ID
                trackWrite("messages/${message.id} → read:true", "markAsRead")
            } catch (e: Exception) {
                Log.e("ChatRepo", "❌ Failed to mark message as read", e)
            }
        }
    }

    /////////////ROOM Database///////////////

    fun observeMessagesForUser(uid: String): Flow<List<LocalMessage>> {
        //trackRead("room/messages/$uid", "observeMessagesForUser")
        return messageDao.getMessagesForUser(uid)
    }

   fun observeWrittenToday(): Flow<List<WrittenTodayEntity>> {
        //trackRead("room/writtenToday", "observeWrittenToday")
        return messageDao.observeWrittenToday()
    }

    suspend fun syncMessages(uid: String): Boolean {
        val now = System.currentTimeMillis()
        val cutoffMillis = now - 24 * 60 * 60 * 1000
        val cutoff = Timestamp(Date(cutoffMillis))
        Log.d("Dumb_stuff", "Bla test")
        return try {
            messageDao.deleteExpiredMessages(cutoffMillis)

            val snapshot = db.collection("messages")
                .whereEqualTo("receiverId", uid)
                .whereGreaterThan("timestamp", cutoff)
                .get()
                .await()

            val newMessages = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            }

            // 🔍 Add this log right after newMessages is created
            newMessages.forEach {
                Log.d("IDCHECK", "Parsed Message: id='${it.id}', content='${it.content}'")
            }

            val localMessages = newMessages.map { it.toLocal() }

            messageDao.insertAll(localMessages)
            trackRead("messages (fresh only)", "syncMessages")
            true
        } catch (e: Exception) {
            Log.e("ChatRepo", "syncMessages failed", e)
            false
        }
    }

    suspend fun recordWrittenUser(receiverId: String) {
        val now = System.currentTimeMillis()
        trackWrite("room/writtenToday/$receiverId", "recordWrittenUser")
        messageDao.insertWrittenEntry(WrittenTodayEntity(receiverId, now))
    }

    suspend fun hasAlreadyWrittenTo(receiverId: String): Boolean {
        trackRead("room/writtenToday/$receiverId", "hasAlreadyWrittenTo")
        return messageDao.getWrittenEntry(receiverId) != null
    }

    suspend fun resetWrittenIfNewDay() {
        val entries = messageDao.getWrittenToday()
        Log.d("ChatRepo", "entries are empty")
        if (entries.isEmpty()) return

        val firstWrite = entries.minOfOrNull { it.timestamp } ?: return

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        if (firstWrite < startOfToday) {
            Log.d("ChatRepo", "New UTC day detected. Clearing WrittenTodayEntity.")
            trackWrite("room/writtenToday/clear", "resetWrittenIfNewDay")
            messageDao.clearWrittenToday()
        } else {
            Log.d("ChatRepo", "Still same UTC day. Keeping written entries.")
        }
    }

    suspend fun hardResetWritten() {
        trackWrite("room/writtenToday/clear", "hardResetWritten")
        messageDao.clearWrittenToday()
    }

    suspend fun fetchMessageById(messageId: String): Message? =
        try {
            val doc = db.collection("messages").document(messageId).get().await()
            doc.toObject(Message::class.java)?.copy(id = doc.id)
        } catch (e: Exception) { Log.e("ChatRepo","fetchMessageById failed", e); null }


    fun Message.toLocal(): LocalMessage = LocalMessage(
        id = this.id,
        senderId = this.senderId,
        senderUsername = this.senderUsername,
        senderAvatarId = this.senderAvatarId,
        senderMood = this.senderMood,
        receiverId = this.receiverId,
        content = this.content,
        timestamp = this.timestamp?.toDate()?.time ?: 0L,
        read = this.read,
        feedback = this.feedback
    )

    // ---------- FAVORITES (new) ----------
    fun observeFavoriteMessages(): Flow<List<LocalFavoriteMessage>> =
        favoriteMessageDao.observeAll()

    suspend fun saveMessageToFavorites(msg: LocalMessage) {
        val fav = LocalFavoriteMessage(
            id = msg.id,
            senderId = msg.senderId,
            senderUsername = msg.senderUsername,
            senderAvatarId = msg.senderAvatarId,
            senderMood = msg.senderMood,
            receiverId = msg.receiverId,
            content = msg.content,
            originalTimestamp = msg.timestamp,
            savedAt = System.currentTimeMillis()
        )
        favoriteMessageDao.upsert(fav)
        trackWrite("room/LocalFavoriteMessage/${msg.id}", "saveMessageToFavorites")
    }

    suspend fun removeFavorite(messageId: String) {
        favoriteMessageDao.deleteById(messageId)
        trackWrite("room/LocalFavoriteMessage/$messageId", "removeFavorite")
    }

    suspend fun isFavorite(messageId: String): Boolean =
        favoriteMessageDao.countById(messageId) > 0


    // ---------- REPORTS -------------

    suspend fun reportMessage(messageId: String, reason: ReportReason, notes: String? = null): ReportResult {
        return try {
            if (messageId.isBlank()) return ReportResult.Invalid

            val payload = hashMapOf(
                "messageId" to messageId,
                "reason" to reason.code,
                "notes" to (notes?.take(300) ?: "") // backend trims to 300 as well
            )

            val result = Firebase.functions("europe-west3")
                .getHttpsCallable("reportMessage")
                .call(payload)
                .await()

            @Suppress("UNCHECKED_CAST")
            val data = result.data as? Map<String, Any?> ?: return ReportResult.Error("No data")

            val success = (data["success"] as? Boolean) == true
            val duplicate = (data["duplicate"] as? Boolean) == true

            trackWrite("cf:reportMessage(${reason.code}) → $messageId", "reportMessage")

            when {
                duplicate -> ReportResult.Duplicate
                success -> ReportResult.Success
                else -> ReportResult.Error("Unknown response")
            }
        } catch (e: FirebaseFunctionsException) {
            Log.e("ReportMessage", "❌ reportMessage failed: ${e.code}", e)
            when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> ReportResult.Error("Unauthenticated")
                FirebaseFunctionsException.Code.INVALID_ARGUMENT -> ReportResult.Invalid
                FirebaseFunctionsException.Code.NOT_FOUND -> ReportResult.Invalid
                FirebaseFunctionsException.Code.ALREADY_EXISTS -> ReportResult.Duplicate
                else -> ReportResult.Error(e.message)
            }
        } catch (e: Exception) {
            Log.e("ReportMessage", "❌ reportMessage unexpected failure", e)
            ReportResult.Error(e.message)
        }
    }

    // Convenience overload if you report from a LocalMessage
    suspend fun reportMessage(message: LocalMessage, reason: ReportReason, notes: String? = null): ReportResult {
        return reportMessage(message.id, reason, notes)
    }

    // ====== ADMIN / MODERATION ======

    suspend fun hasAdminAccess(): Boolean {
        return try {
            val res = Firebase.functions("europe-west3")
                .getHttpsCallable("isAdminSelf")
                .call(emptyMap<String, Any>())
                .await()
            val data = res.data as? Map<*, *> ?: return false
            (data["isAdmin"] as? Boolean) == true
        } catch (e: Exception) {
            Log.e("Admin123", "isAdminSelf failed", e)
            false
        }
    }

    /** Fetch a page of reports. status = "open" | "closed" */
    suspend fun fetchReports(status: String = "open", limit: Int = 50): List<AdminReportItem> {
        return try {
            val res = Firebase.functions("europe-west3")
                .getHttpsCallable("listReports")
                .call(mapOf("status" to status, "limit" to limit))
                .await()

            val payload = res.data as? Map<*, *> ?: return emptyList()
            val items = payload["reports"] as? List<Map<*, *>> ?: emptyList()

            items.map { m ->
                AdminReportItem(
                    id = m["id"] as? String ?: "",
                    status = m["status"] as? String ?: "open",
                    createdAt = parseTs(m["createdAt"]),
                    reason = m["reason"] as? String,
                    reporterId = m["reporterId"] as? String,
                    reporterUsername = m["reporterUsername"] as? String,
                    offenderId = m["offenderId"] as? String,
                    offenderUsername = m["offenderUsername"] as? String,
                    messageId = m["messageId"] as? String,
                    messagePreview = m["messagePreview"] as? String,
                    messageTimestamp = parseTs(m["messageTimestamp"]),
                    actionTaken = m["actionTaken"] as? String,

                    offenderWarnCount = (m["offenderWarnCount"] as? Number)?.toInt(),
                    offenderBanCount = (m["offenderBanCount"] as? Number)?.toInt(),
                    offenderLastWarnedAt = parseTs(m["offenderLastWarnedAt"]),
                    offenderLastBannedAt = parseTs(m["offenderLastBannedAt"]),
                )
            }.also {
                trackRead("cf:listReports($status) -> ${it.size}", "fetchReports")
            }
        } catch (e: Exception) {
            Log.e("Admin123", "fetchReports failed", e)
            emptyList()
        }
    }

    private fun parseTs(v: Any?): com.google.firebase.Timestamp? = when (v) {
        is com.google.firebase.Timestamp -> v
        is Map<*, *> -> {
            val s = (v["_seconds"] as? Number)?.toLong()
            val ns = (v["_nanoseconds"] as? Number)?.toInt() ?: 0
            if (s != null) com.google.firebase.Timestamp(s, ns) else null
        }
        is Number -> {
            // treat as millis
            val ms = v.toLong()
            com.google.firebase.Timestamp(ms / 1000, ((ms % 1000) * 1_000_000).toInt())
        }
        else -> null
    }


    /** Close/warn/ban in one shot via takeModerationAction. action = "none" | "warn" | "ban" */
    suspend fun takeModerationAction(
        reportId: String,
        action: String,
        banHours: Int? = null,
        note: String? = null
    ): Boolean {
        return try {
            val payload = mutableMapOf<String, Any>(
                "reportId" to reportId,
                "action" to action
            )
            if (banHours != null) payload["banHours"] = banHours
            if (!note.isNullOrBlank()) payload["note"] = note.take(500)

            val res = Firebase.functions("europe-west3")
                .getHttpsCallable("takeModerationAction")
                .call(payload)
                .await()

            val ok = ((res.data as? Map<*, *>)?.get("success") as? Boolean) == true
            if (ok) trackWrite("cf:takeModerationAction($action,$banHours) → $reportId", "takeModerationAction")
            ok
        } catch (e: FirebaseFunctionsException) {
            Log.e("Admin", "takeModerationAction failed: ${e.code}", e)
            false
        } catch (e: Exception) {
            Log.e("Admin", "takeModerationAction failed", e)
            false
        }
    }

    /** Optional: direct ban/unban by user id (unban = hours=0) */
    suspend fun setBanStatus(userId: String, hours: Int): Boolean {
        return try {
            val res = Firebase.functions("europe-west3")
                .getHttpsCallable("setBanStatus")
                .call(mapOf("uid" to userId, "hours" to hours))
                .await()
            val ok = ((res.data as? Map<*, *>)?.get("success") as? Boolean) == true
            if (ok) trackWrite("cf:setBanStatus($userId,$hours)", "setBanStatus")
            ok
        } catch (e: Exception) {
            Log.e("Admin", "setBanStatus failed", e)
            false
        }
    }

}

