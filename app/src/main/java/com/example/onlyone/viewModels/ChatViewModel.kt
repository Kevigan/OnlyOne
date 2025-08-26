package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlyone.cloudMessaging.MessageNotifier
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.WrittenTodayEntity
import com.example.onlyone.repos.ChatRepository
import com.example.onlyone.repos.userRepos.UserRepository
import com.example.onlyone.utils.DailyResetTimer
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _receivedMessages = MutableStateFlow<List<Message>>(emptyList())
    val receivedMessages: StateFlow<List<Message>> = _receivedMessages

    val writtenTodayList: Flow<List<WrittenTodayEntity>> = chatRepository.observeWrittenToday()

    private val _userQueue = MutableStateFlow<List<PublicUser>>(emptyList())
    val userQueue: StateFlow<List<PublicUser>> = _userQueue.asStateFlow()

    val messageFlow = MessageNotifier.newMessageFlow

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()


    init {
        DailyResetTimer.start {
            resetWrittenTodayIfNeeded()
        }
    }

    fun sendMessage(
        message: Message,
        userViewModel: UserViewModel,
        onComplete: (MessageResult) -> Unit
    ) {
        _isSending.value = true

        viewModelScope.launch {
            val result = chatRepository.sendMessage(message)

            if (result is MessageResult.Success) {
                val alreadySent = chatRepository.hasAlreadyWrittenTo(message.receiverId)
                if (!alreadySent) {
                    chatRepository.recordWrittenUser(message.receiverId)
                    Log.d("SendMessage", "📝 Marked user as written to: ${message.receiverId}")
                }
            }
            userViewModel.loadUser()
            onComplete(result)
            _isSending.value = false
        }
    }

    fun addFeedback(message: LocalMessage, feedback: Int) {
        viewModelScope.launch {
            chatRepository.addFeedback(message, feedback)
        }
    }

    fun markMessageAsRead(message: LocalMessage) {
        viewModelScope.launch {
            if (!message.read) {
                chatRepository.markAsRead(message)
            }
        }
    }

    //////////////ROOM Database////////////////////

    fun observeLocalMessages(uid: String): Flow<List<LocalMessage>> {
        return chatRepository.observeMessagesForUser(uid)
    }

    suspend fun syncMessagesFromServer(uid: String): Boolean {
        return chatRepository.syncMessages(uid)
    }

    fun resetWrittenTodayIfNeeded() {
        viewModelScope.launch {
            chatRepository.resetWrittenIfNewDay()
        }
    }

    fun hardResetWritten(){
        viewModelScope.launch {
            chatRepository.hardResetWritten()
        }
    }

    fun canSendTo(receiverId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val alreadySent = chatRepository.hasAlreadyWrittenTo(receiverId)
            onResult(!alreadySent)
        }
    }

    // ---------- FAVORITES (new) ----------
    fun observeFavoriteMessages() = chatRepository.observeFavoriteMessages()

    fun saveMessageToFavorites(msg: LocalMessage) {
        viewModelScope.launch { chatRepository.saveMessageToFavorites(msg) }
    }

    fun removeFavorite(messageId: String) {
        viewModelScope.launch { chatRepository.removeFavorite(messageId) }
    }

    suspend fun isFavorite(messageId: String): Boolean =
        chatRepository.isFavorite(messageId)
}
