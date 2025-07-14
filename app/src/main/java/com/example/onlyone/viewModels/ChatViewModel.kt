package com.example.onlyone.viewModels

import androidx.lifecycle.ViewModel
import com.example.onlyone.data.Message
import com.example.onlyone.repos.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

   /* fun sendMessage(message: Message, onComplete: (Boolean) -> Unit) {
        chatRepository.sendMessage(message).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    fun checkDailyMessageLimit(uid: String, onResult: (Int) -> Unit) {
        chatRepository.getMessagesForUserToday(uid).addOnSuccessListener {
            onResult(it.size())
        }
    }

    fun addFeedback(messageId: String, feedback: Int) {
        chatRepository.addFeedback(messageId, feedback)
    }*/
}
