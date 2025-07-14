package com.example.onlyone.repos

import com.example.onlyone.data.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository@Inject constructor(/*private val db: FirebaseFirestore*/) {

    /*fun sendMessage(message: Message): Task<Void> {
        val docId = db.collection("messages").document().id
        val msgWithId = message.copy(id = docId)
        return db.collection("messages").document(docId).set(msgWithId)
    }

    fun getMessagesForUserToday(uid: String): Task<QuerySnapshot> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        return db.collection("messages")
                .whereEqualTo("senderId", uid)
                .whereGreaterThan("timestamp", startOfDay)
                .get()
    }

    fun addFeedback(messageId: String, feedback: Int): Task<Void> {
        return db.collection("messages").document(messageId).update("feedback", feedback)
    }*/

    // Add logic for auto-deletion after 24h if needed via Cloud Functions
}
