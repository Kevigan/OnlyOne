package com.onlyone.app.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyone.app.cloudMessaging.MessageNotifier
import com.onlyone.app.data.AdminReportItem
import com.onlyone.app.data.LocalFavoriteMessage
import com.onlyone.app.data.LocalMessage
import com.onlyone.app.data.Message
import com.onlyone.app.data.MessageResult
import com.onlyone.app.data.PublicUser
import com.onlyone.app.data.ReportReason
import com.onlyone.app.data.ReportResult
import com.onlyone.app.data.WrittenTodayEntity
import com.onlyone.app.repos.ChatRepository
import com.onlyone.app.repos.userRepos.UserRepository
import com.onlyone.app.utils.DailyResetTimer
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ChatViewModel
 *
 * Owns messaging, favorites, daily 1-per-user send tracking, and basic moderation actions.
 * Keep anything that talks to repositories inside viewModelScope so UI stays dumb & reactive.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository, // kept for future use (profile gates, etc.)
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────────────────────
    // Realtime new-message notifications (FCM → app)
    // ─────────────────────────────────────────────────────────────────────────────
    val messageFlow = MessageNotifier.newMessageFlow

    // ─────────────────────────────────────────────────────────────────────────────
    // Send state (UI disables send buttons while true)
    // ─────────────────────────────────────────────────────────────────────────────
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // One-off toasts or lightweight user messages (collect in UI when needed)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    // ─────────────────────────────────────────────────────────────────────────────
    // "Written today" (enforces 1 message per recipient per day)
    // ─────────────────────────────────────────────────────────────────────────────
    // Note: Room/Repo should emit only when changed; we still distinctUntilChanged for safety.
    val writtenTodayList: Flow<List<WrittenTodayEntity>> =
        chatRepository.observeWrittenToday().distinctUntilChanged()

    /**
     * Can the current user send to [receiverId] today?
     * Callback used to avoid leaking repository details into UI.
     */
    fun canSendTo(receiverId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val alreadySent = chatRepository.hasAlreadyWrittenTo(receiverId)
            onResult(!alreadySent)
        }
    }

    /**
     * Resets "written today" state when the day rolls over.
     * DailyResetTimer will call this once per day.
     */
    private fun resetWrittenTodayIfNeeded() {
        viewModelScope.launch { chatRepository.resetWrittenIfNewDay() }
    }

    /** Manual admin/dev helper to clear today's written cache. */
    fun hardResetWritten() {
        viewModelScope.launch { chatRepository.hardResetWritten() }
    }

    init {
        // Start the global daily timer → call reset when date changes.
        DailyResetTimer.start { resetWrittenTodayIfNeeded() }
        resetWrittenTodayIfNeeded()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Local (24h) inbox messages
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Observe local messages (Room) for a given user [uid].
     * We apply distinctUntilChanged to avoid redundant recompositions.
     */
    fun observeLocalMessages(uid: String): Flow<List<LocalMessage>> =
        chatRepository.observeMessagesForUser(uid).distinctUntilChanged()

    /**
     * Pull fresh messages from server into Room.
     * Returns true on success to allow the UI to show an error state.
     */
    suspend fun syncMessagesFromServer(uid: String): Boolean =
        try { chatRepository.syncMessages(uid) }
        catch (t: Throwable) { false }

    /**
     * Mark a message as read (no-op if already read).
     */
    fun markMessageAsRead(message: LocalMessage) {
        if (message.read) return
        viewModelScope.launch { chatRepository.markAsRead(message) }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Sending messages
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Sends a message and records the "written-to" recipient for the day if it was new.
     * We also refresh the user object after sending (achievements / currencies, etc.).
     */
    fun sendMessage(
        message: Message,
        userViewModel: UserViewModel,
        onComplete: (MessageResult) -> Unit
    ) {
        viewModelScope.launch {
            _isSending.value = true
            try {
                when (val result = chatRepository.sendMessage(message)) {
                    is MessageResult.Success -> {
                        // mark written today (unchanged)
                        //val alreadySent = chatRepository.hasAlreadyWrittenTo(message.receiverId)
                        //if (!alreadySent) chatRepository.recordWrittenUser(message.receiverId)

                        // ✅ optimistic user update
                        userViewModel.applySendRewards(
                            goldDelta = result.gold ?: 0,
                            pointsDelta = result.points ?: 0,
                            rune = result.rune
                        )

                        // ✅ cheap reconciliation from source-of-truth (no friends/queues/etc.)
                        userViewModel.fetchUserInventory()

                        onComplete(result)
                    }
                    MessageResult.AlreadySent -> onComplete(MessageResult.AlreadySent)
                    MessageResult.Error -> onComplete(MessageResult.Error)
                }
            } catch (_: Throwable) {
                // Map unexpected exceptions to your Error object
                onComplete(MessageResult.Error)
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Add simple feedback to a received message (👍/👎/😐 mapped to ints).
     */
    fun addFeedback(message: LocalMessage, feedback: Int) {
        viewModelScope.launch { chatRepository.addFeedback(message, feedback) }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Favorites (saved messages)
    // ─────────────────────────────────────────────────────────────────────────────
    fun observeFavoriteMessages(): Flow<List<LocalFavoriteMessage>> =
        chatRepository.observeFavoriteMessages().distinctUntilChanged()

    fun saveMessageToFavorites(msg: LocalMessage) {
        viewModelScope.launch { chatRepository.saveMessageToFavorites(msg) }
    }

    fun removeFavorite(messageId: String) {
        viewModelScope.launch { chatRepository.removeFavorite(messageId) }
    }

    suspend fun isFavorite(messageId: String): Boolean =
        chatRepository.isFavorite(messageId)

    // ─────────────────────────────────────────────────────────────────────────────
    // Moderation / Reports (admin-only UI uses these)
    // ─────────────────────────────────────────────────────────────────────────────
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _reports = MutableStateFlow<List<AdminReportItem>>(emptyList())
    val reports: StateFlow<List<AdminReportItem>> = _reports.asStateFlow()

    private val _reportsLoading = MutableStateFlow(false)
    val reportsLoading: StateFlow<Boolean> = _reportsLoading.asStateFlow()

    // Simple in-memory guards to avoid duplicate admin checks
    private var adminCheckLoaded = false
    private var adminCheckLoading = false

    /**
     * Check whether the current user has admin access.
     * Cached once per VM lifecycle unless [force] is true.
     */
    fun checkAdminAccess(force: Boolean = false, onLoaded: ((Boolean) -> Unit)? = null) {
        if (!force) {
            if (adminCheckLoaded) {
                onLoaded?.invoke(_isAdmin.value)
                return
            }
            if (adminCheckLoading) return
        }

        adminCheckLoading = true
        viewModelScope.launch {
            try {
                val isAdminNow = chatRepository.hasAdminAccess()
                _isAdmin.value = isAdminNow
                onLoaded?.invoke(isAdminNow)
            } finally {
                adminCheckLoaded = true
                adminCheckLoading = false
            }
        }
    }

    /**
     * Fetch moderation reports (status: "open" | "resolved" | ...).
     */
    fun loadReports(status: String = "open", limit: Int = 50) {
        viewModelScope.launch {
            _reportsLoading.value = true
            try {
                _reports.value = chatRepository.fetchReports(status, limit)
            } finally {
                _reportsLoading.value = false
            }
        }
    }

    /**
     * Report a message and return the backend result via callback.
     */
    fun reportMessageAndReturn(
        message: LocalMessage,
        reason: ReportReason,
        notes: String? = null,
        onResult: (ReportResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = chatRepository.reportMessage(message, reason, notes)
                onResult(res)
            } catch (t: Throwable) {
                // If your ReportResult has a dedicated error type, map to it here.
                // Otherwise, just no-op or log. Keeping callback contract intact:
                onResult(ReportResult.Error(t.message ?: "Report failed"))
                // If you don't have ReportResult.Error, remove the line above and log instead.
                // Log.e("ChatViewModel", "reportMessage failed", t)
            }
        }
    }


    /**
     * Resolve a report with an action:
     *  - action: "none" | "warn" | "ban"
     *  - if "ban", pass [banHours]
     */
    fun resolveReport(
        reportId: String,
        action: String,
        banHours: Int? = null,
        note: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ok = chatRepository.takeModerationAction(reportId, action, banHours, note)
            if (ok) {
                // Refresh the open list after action
                loadReports("open", 50)
            }
            onDone(ok)
        }
    }

    /**
     * Directly set a user's ban status.
     * Pass hours=0 to unban.
     */
    fun setUserBan(userId: String, hours: Int, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val ok = chatRepository.setBanStatus(userId, hours)
            onDone(ok)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Legacy / reserved (kept to avoid breaking callers)
    // ─────────────────────────────────────────────────────────────────────────────
    // If you don't use this anywhere, consider removing to reduce surface area.
    private val _receivedMessages = MutableStateFlow<List<Message>>(emptyList())
    val receivedMessages: StateFlow<List<Message>> = _receivedMessages.asStateFlow()

    // Future discovery queue (kept for parity; populate via repository when used)
    private val _userQueue = MutableStateFlow<List<PublicUser>>(emptyList())
    val userQueue: StateFlow<List<PublicUser>> = _userQueue.asStateFlow()
}
