package com.example.onlyone.viewModels.userViewModel

import android.util.Log
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.data.UserEngagementStatus
import com.example.onlyone.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SwipeManager(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val getUser: () -> UserComposite?,
    private val getEngagement: () -> UserEngagementStatus?,
    private val updateEngagement: (UserEngagementStatus) -> Unit,
    private val refreshEngagementStatus: () -> Unit
) {
    private val _userQueue = MutableStateFlow<List<PublicUser>>(emptyList())
    val userQueue: StateFlow<List<PublicUser>> = _userQueue.asStateFlow()

    private val _targetUser = MutableStateFlow<PublicUser?>(null)
    val targetUser: StateFlow<PublicUser?> = _targetUser.asStateFlow()

    private val _isLoadingUserBatch = MutableStateFlow(false)
    val isLoadingUserBatch: StateFlow<Boolean> = _isLoadingUserBatch.asStateFlow()

    private val _lastUserLoadResult = MutableStateFlow<RandomUserLoadResult?>(null)
    val lastUserLoadResult: StateFlow<RandomUserLoadResult?> = _lastUserLoadResult.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private var hasLoadedInitialBatch = false
    private var lastLoadedMessageUid: String? = null

    fun loadRandomUserBatchIfNeeded(showToasts: Boolean = false) {
        if (hasLoadedInitialBatch) return
        hasLoadedInitialBatch = true
        viewModelScope.launch {
            loadRandomUserBatch(showToasts)
        }
    }

    suspend fun loadRandomUserBatch(showToasts: Boolean = false): RandomUserLoadResult {
        _isLoadingUserBatch.value = true

        val result = try {
            val currentUser = getUser()
            val engagement = getEngagement()

            if (currentUser == null || engagement == null) {
                val fallback = RandomUserLoadResult.NoSwipesLeft
                _lastUserLoadResult.value = fallback
                return fallback
            }

            val swipesLeft = currentUser.maxSwipes - engagement.swipesUsed
            if (swipesLeft <= 0) {
                if (showToasts) _toastEvent.emit("🚫 No swipes left today.")
                val noSwipes = RandomUserLoadResult.NoSwipesLeft
                _lastUserLoadResult.value = noSwipes
                return noSwipes
            }

            val writtenToday = userRepository.observeWrittenToday().first().map { it.receiverId }
            val searchLanguage = userRepository.getSearchUserLanguage(currentUser.uid)

            val randomUsers = userRepository.loadRandomUserBatchSuspend(
                excludedIds = writtenToday,
                chatLanguage = searchLanguage
            )

            if (randomUsers.isNotEmpty()) {
                _userQueue.value = randomUsers
                _targetUser.value = randomUsers.first()
                RandomUserLoadResult.Success.also { _lastUserLoadResult.value = it }
            } else {
                _userQueue.value = emptyList()
                _targetUser.value = null

                if (showToasts) {
                    if (searchLanguage != "any") {
                        _toastEvent.emit("No users found in selected language.")
                    } else {
                        _toastEvent.emit("🎉 You've seen everyone for now.")
                    }
                }

                RandomUserLoadResult.NoUsersFound.also { _lastUserLoadResult.value = it }
            }

        } finally {
            _isLoadingUserBatch.value = false
        }

        return result
    }

    fun consumeNextUserFromQueue() {
        val user = getUser()
        val engagement = getEngagement()

        if (user == null || engagement == null) return

        viewModelScope.launch {
            val swipesLeft = user.maxSwipes - engagement.swipesUsed
            if (swipesLeft <= 0) {
                _toastEvent.emit("🚫 No swipes left today.")
                return@launch
            }

            val desiredLanguage = userRepository.getSearchUserLanguage(user.uid)
            val remainingUsers = _userQueue.value.drop(1)
            val nextMatch = remainingUsers.firstOrNull {
                it.chatLanguage == desiredLanguage || desiredLanguage == "any"
            }

            val success = userRepository.incrementSwipeCount()
            if (!success) {
                Log.e("SwipeManager", "❌ Failed to increment swipe count")
            }

            updateEngagement(engagement.copy(swipesUsed = engagement.swipesUsed + 1))

            if (nextMatch != null) {
                _userQueue.value = remainingUsers
                _targetUser.value = nextMatch
            } else {
                _userQueue.value = emptyList()
                _targetUser.value = null

                if (getEngagement()?.swipesUsed == user.maxSwipes) {
                    refreshEngagementStatus()
                }

                when (val result = loadRandomUserBatch()) {
                    is RandomUserLoadResult.NoUsersFound -> {
                        _toastEvent.emit("🎉 You've seen everyone for now.")
                    }
                    is RandomUserLoadResult.NoSwipesLeft -> {
                        _toastEvent.emit("🚫 You've hit your daily swipe limit.")
                    }
                    else -> {}
                }
            }
        }
    }

    fun setTargetUser(user: PublicUser) {
        _targetUser.value = user
    }


    sealed class RandomUserLoadResult {
        object Success : RandomUserLoadResult()
        object NoUsersFound : RandomUserLoadResult()
        object NoSwipesLeft : RandomUserLoadResult()
    }
}
