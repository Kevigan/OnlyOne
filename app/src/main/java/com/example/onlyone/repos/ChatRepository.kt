package com.example.onlyone.repos

import android.util.Log
import com.example.dao.MessageDao
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.utils.toFirestoreMap
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.WrittenTodayEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val messageDao: MessageDao
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

            return when {
                alreadySent -> MessageResult.AlreadySent
                success -> {
                    val rewards = response["rewards"] as? Map<*, *>
                    val gold = rewards?.get("gold") as? Int ?: 0
                    val points = rewards?.get("points") as? Int ?: 0
                    val rune = rewards?.get("runeEarned") as? String

                    Log.d("SendMessage", "✅ Rewards: $gold gold, $points points, rune: $rune")

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

    suspend fun addFeedbackOffline(messageId: String, feedback: Int) {
        try {
            // Update feedback in Firestore
            db.collection("messages").document(messageId)
                .update("feedback", feedback)
                .await()

            // Update local Room entry (optional: ensure it exists)
            val localMessage = messageDao.getMessageById(messageId)
            if (localMessage != null) {
                val updated = localMessage.copy(feedback = feedback)
                messageDao.insertAll(listOf(updated))
            }

            trackWrite("messages/$messageId → feedback:$feedback", "addFeedbackOffline")
        } catch (e: Exception) {
            Log.e("ChatRepo", "❌ addFeedbackOffline failed", e)
            throw e
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
}

