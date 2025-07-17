package com.example.onlyone.repos

import android.util.Log
import com.example.onlyone.data.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val db: FirebaseFirestore) {

    private var readCount = 0
    private var writeCount = 0

    private fun trackRead(path: String, from: String = "") {
        readCount++
        Log.d("FirestoreTrack", "Read [$readCount]: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    private fun trackWrite(path: String, from: String = "") {
        writeCount++
        Log.d("FirestoreTrack", "Write [$writeCount]: $path${if (from.isNotBlank()) " ($from)" else ""}")
    }

    fun sendMessage(message: Message): Task<Void> {
        val docId = db.collection("messages").document().id
        val msgWithId = message.copy(id = docId)

        trackWrite("messages/$docId", "sendMessage")

        return db.collection("messages").document(docId).set(msgWithId)
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
}

//recipientId