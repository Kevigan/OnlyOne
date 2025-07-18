package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.Message
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.WrittenTodayEntity
import com.example.onlyone.repos.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _targetUser = MutableStateFlow<PublicUser?>(null)
    val targetUser: StateFlow<PublicUser?> = _targetUser.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<Message>>(emptyList())
    val receivedMessages: StateFlow<List<Message>> = _receivedMessages

    val writtenTodayList: Flow<List<WrittenTodayEntity>> = chatRepository.observeWrittenToday()

    private val _timeUntilReset = MutableStateFlow(getMillisUntilNextUtcMidnight())
    val timeUntilReset: StateFlow<Long> = _timeUntilReset.asStateFlow()

    init {
        startResetCountdown()
    }

    private fun startResetCountdown() {
        viewModelScope.launch {
            while (true) {
                val millis = getMillisUntilNextUtcMidnight()
                _timeUntilReset.value = millis

                if (millis <= 1_000L) {
                    resetWrittenTodayIfNeeded()
                }

                delay(60_000) // check every 60 seconds
            }
        }
    }

    private fun getMillisUntilNextUtcMidnight(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DATE, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    fun sendMessage(message: Message, onComplete: (Boolean) -> Unit) {
        chatRepository.sendMessage(message).addOnCompleteListener {
            val success = it.isSuccessful
            if (success) {
                viewModelScope.launch {
                    chatRepository.recordWrittenUser(message.receiverId)
                }
            }
            onComplete(success)
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


    //////////////ROOM Database////////////////////

    fun observeLocalMessages(uid: String): Flow<List<LocalMessage>> {
        return chatRepository.observeMessagesForUser(uid)
    }

    fun syncMessagesFromServer(uid: String) {
        viewModelScope.launch {
            chatRepository.syncMessages(uid)
        }
    }

    fun resetWrittenTodayIfNeeded() {
        viewModelScope.launch {
            chatRepository.resetWrittenIfNewDay()
        }
    }

    fun canSendTo(receiverId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val alreadySent = chatRepository.hasAlreadyWrittenTo(receiverId)
            onResult(!alreadySent)
        }
    }
}
