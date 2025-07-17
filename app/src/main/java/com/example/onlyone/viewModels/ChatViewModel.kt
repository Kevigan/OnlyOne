package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.onlyone.data.Message
import com.example.onlyone.data.PublicUser
import com.example.onlyone.repos.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _targetUser = MutableStateFlow<PublicUser?>(null)
    val targetUser: StateFlow<PublicUser?> = _targetUser.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<Message>>(emptyList())
    val receivedMessages: StateFlow<List<Message>> = _receivedMessages

    fun loadReceivedMessages(userId: String) {
        Log.d("ChatVM", "loadReceivedMessages called for $userId")
        chatRepository.getMessagesToUser(userId)
            .addOnSuccessListener { snapshot ->
                Log.d("ChatVM", "Snapshot returned ${snapshot.size()} messages")

                if (snapshot.isEmpty) {
                    Log.w("ChatVM", "No messages found for recipientId=${userId}")
                }

                val messages = snapshot.documents.mapNotNull { doc ->
                    val raw = doc.data
                    Log.d("ChatVM", "Raw doc: $raw")
                    doc.toObject(Message::class.java)
                }

                _receivedMessages.value = messages
            }
            .addOnFailureListener {
                Log.w("ChatVM", "Failed to load received messages", it)
                _receivedMessages.value = emptyList()
            }
    }

    fun sendMessage(message: Message, onComplete: (Boolean) -> Unit) {
        chatRepository.sendMessage(message).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    fun checkDailyMessageLimit(uid: String, onResult: (Int) -> Unit) {
        chatRepository.getMessagesToUser(uid).addOnSuccessListener {
            onResult(it.size())
        }
    }

    fun addFeedback(messageId: String, feedback: Int) {
        chatRepository.addFeedback(messageId, feedback)
    }

    fun loadTargetUser(
        uid: String,
        isFriend: Boolean,
        userViewModel: UserViewModel
    ) {
        if (isFriend) {
            // Try pulling from cached friends
            val cached = userViewModel.friends.value.firstOrNull { it.uid == uid }
            if (cached != null) {
                _targetUser.value = cached
                return
            }
        }

        // Not cached, pull from repo
        userViewModel.repository.getPublicUser(uid).addOnSuccessListener { doc ->
            val user = doc.toObject(PublicUser::class.java)
            _targetUser.value = user
        }.addOnFailureListener {
            _targetUser.value = null
        }
    }
}
