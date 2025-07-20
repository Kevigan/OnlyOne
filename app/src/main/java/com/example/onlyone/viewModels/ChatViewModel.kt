package com.example.onlyone.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.Message
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.WrittenTodayEntity
import com.example.onlyone.repos.ChatRepository
import com.example.onlyone.repos.UserRepository
import com.example.onlyone.utils.toPublicUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _userQueue = MutableStateFlow<List<PublicUser>>(emptyList())
    val userQueue: StateFlow<List<PublicUser>> = _userQueue.asStateFlow()

    private val _isLoadingUser = MutableStateFlow(false)
    val isLoadingUser: StateFlow<Boolean> = _isLoadingUser.asStateFlow()


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
        chatRepository.sendMessage(message)
            .addOnCompleteListener {
                val success = it.isSuccessful
                if (success) {
                    viewModelScope.launch {
                        chatRepository.recordWrittenUser(message.receiverId)
                    }
                }
                onComplete(success)
            }
            .addOnFailureListener { e ->
                Log.e("SendMessage", "❌ Firestore write failed: ${e.message}", e)
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


    fun loadRandomUserBatch(
        currentUserId: String,
        userRepository: UserRepository,
        onNotEnoughSwipes: () -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        userRepository.getSwipeStatus(currentUserId) { swipeStatus ->
            if (swipeStatus == null) {
                onNotEnoughSwipes()
                onComplete(false)
                return@getSwipeStatus
            }

            val swipesLeft = swipeStatus.swipesGranted - swipeStatus.swipesUsed
            if (swipesLeft <= 0) {
                onNotEnoughSwipes()
                onComplete(false)
                return@getSwipeStatus
            }

            // ✅ Continue if swipes available
            viewModelScope.launch {
                val writtenToday = writtenTodayList.first().map { it.receiverId }

                userRepository.getRandomUsersFromCloud(writtenToday) { users ->
                    if (!users.isNullOrEmpty()) {
                        _userQueue.value = users
                        _targetUser.value = users.first() // preload first for UI
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun consumeNextUserFromQueue() {
        val currentList = _userQueue.value
        if (currentList.isNotEmpty()) {
            val newList = currentList.drop(1)
            _userQueue.value = newList
            _targetUser.value = newList.firstOrNull()
        } else {
            _targetUser.value = null
        }
    }

    fun loadTargetUser(
        uid: String,
        isRandom: Boolean,
        userViewModel: UserViewModel
    ) {
        Log.d("ChatViewModel", "loadTargetUser called with uid=$uid, isRandom=$isRandom")

        viewModelScope.launch {
            if (!isRandom) {
                val cached = userViewModel.getLocalFriend(uid)
                if (cached != null) {
                    Log.d("ChatViewModel", "Found cached friend for uid=$uid: ${cached.username}")
                    _targetUser.value = cached.toPublicUser()
                    return@launch
                } else {
                    Log.d("ChatViewModel", "No cached friend found for uid=$uid")
                }
            }

            // Not cached or random user → fetch from Firestore
            Log.d("ChatViewModel", "Fetching user $uid from Firestore")
            userViewModel.repository.getPublicUser(uid)
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(PublicUser::class.java)
                    if (user != null) {
                        Log.d("ChatViewModel", "Fetched user from Firestore: ${user.username}")
                    } else {
                        Log.w("ChatViewModel", "User document for $uid exists but couldn't be parsed")
                    }
                    _targetUser.value = user
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Failed to fetch user from Firestore for uid=$uid", e)
                    _targetUser.value = null
                }
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
}
