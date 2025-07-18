package com.example.onlyone.repos

import android.util.Log
import com.example.dao.MessageDao
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.utils.toFirestoreMap
import com.example.onlyone.data.Message
import com.example.onlyone.data.WrittenTodayEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
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
        Log.d("FirestoreTrack", "Read Repo [$readCount]: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    private fun trackWrite(path: String, from: String = "") {
        writeCount++
        Log.d("FirestoreTrack", "Write Repo [$writeCount]: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    fun sendMessage(message: Message): Task<Void> {
        val docId = db.collection("messages").document().id
        val msgWithId = message.copy(id = docId)

        trackWrite("messages/$docId", "sendMessage")

        val messageMap = msgWithId.toFirestoreMap()
        return db.collection("messages").document(docId).set(messageMap)
    }

    fun getMessagesToUser(uid: String): Task<QuerySnapshot> {
        trackRead("messages?recipientId=$uid", "getMessagesToUser")

        return db.collection("messages")
            .whereEqualTo("receiverId", uid)
            .get()
    }

    fun addFeedback(messageId: String, feedback: Int): Task<Void> {
        trackWrite("messages/$messageId → feedback", "addFeedback")

        return db.collection("messages").document(messageId).update("feedback", feedback)
    }

    // Add logic for auto-deletion after 24h if needed via Cloud Functions


    /////////////ROOM Database///////////////

    fun observeMessagesForUser(uid: String): Flow<List<LocalMessage>> {
        return messageDao.getMessagesForUser(uid)
    }

    fun observeWrittenToday(): Flow<List<WrittenTodayEntity>> {
        return messageDao.observeWrittenToday()
    }

    suspend fun syncMessages(uid: String) {
        val lastTimestampMillis = messageDao.getLastTimestamp(uid) ?: 0L
        val lastTimestamp = Timestamp(lastTimestampMillis / 1000, ((lastTimestampMillis % 1000) * 1_000_000).toInt())

        try {
            val snapshot = db.collection("messages")
                .whereEqualTo("receiverId", uid)
                .whereGreaterThan("timestamp", lastTimestamp)
                .get()
                .await()

            val newMessages = snapshot.documents.mapNotNull { doc ->
                val msg = doc.toObject(Message::class.java)
                msg
            }
            Log.d("ChatRepo", "Fetched ${newMessages.size} new messages")

            val localMessages = newMessages.map { it.toLocal() }

            messageDao.insertAll(localMessages)

            trackRead("messages (new only)", "syncMessages")

        } catch (e: Exception) {
            Log.e("ChatRepo", "syncMessages failed", e)
        }
    }

    suspend fun recordWrittenUser(receiverId: String) {
        val now = System.currentTimeMillis()
        messageDao.insertWrittenEntry(WrittenTodayEntity(receiverId, now))
    }

    suspend fun hasAlreadyWrittenTo(receiverId: String): Boolean {
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
            messageDao.clearWrittenToday()
        } else {
            Log.d("ChatRepo", "Still same UTC day. Keeping written entries.")
        }
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

