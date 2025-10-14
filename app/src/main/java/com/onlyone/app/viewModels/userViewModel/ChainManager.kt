// app/src/main/java/com/onlyone/app/viewModels/chain/ChainManager.kt
package com.onlyone.app.viewModels.chain

import android.util.Log
import com.onlyone.app.data.SavedChainEntity
import com.onlyone.app.data.chainMessage.Chain
import com.onlyone.app.data.chainMessage.ChainStep
import com.onlyone.app.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Lightweight manager that owns chain-related state for the current user.
 * Construct it inside UserViewModel with viewModelScope (same pattern as UserManager).
 */
class ChainManager @Inject constructor(
    private val userRepository: UserRepository,
    private val scope: CoroutineScope
) {
    // ---- Public state -------------------------------------------------------

    private val _myChains = MutableStateFlow<List<Chain>>(emptyList())
    val myChains: StateFlow<List<Chain>> = _myChains.asStateFlow()

    private val _assignedToMe = MutableStateFlow<List<Chain>>(emptyList())
    val assignedToMe: StateFlow<List<Chain>> = _assignedToMe.asStateFlow()

    // one-off UI messages (errors, success)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    // ---- Internal -----------------------------------------------------------

    private var observeJobs: List<Job> = emptyList()
    private var currentUid: String? = null

    /**
     * Start (or restart) observing streams for a given user.
     * Call this when the user is known or changes (e.g., after login).
     */
    // ChainManager.start(uid)
    fun start(uid: String) {
        if (currentUid == uid) return
        Log.d("ChainManager", "▶️ start observing for uid=$uid")
        currentUid = uid

        observeJobs.forEach { it.cancel() }
        observeJobs = listOf(
            scope.launch {
                userRepository.observeMyChains(uid)
                    .onStart { Log.d("ChainManager", "👂 observeMyChains started") }
                    .catch { t -> Log.e("ChainManager", "observeMyChains error", t); _myChains.value = emptyList() }
                    .collect {
                        Log.d("ChainManager", "📥 myChains update size=${it.size}")
                        _myChains.value = it
                    }
            },
            /*scope.launch {
                userRepository.observeAssignedChains(uid)
                    .onStart { Log.d("ChainManager", "👂 observeAssignedChains started") }
                    .catch { t -> Log.e("ChainManager", "observeAssignedChains error", t); _assignedToMe.value = emptyList() }
                    .collect {
                        Log.d("ChainManager", "📥 assigned update size=${it.size}")
                        _assignedToMe.value = it
                    }
            }*/
        )
    }

    fun stop() {
        observeJobs.forEach { it.cancel() }
        observeJobs = emptyList()
        currentUid = null
        _myChains.value = emptyList()
        _assignedToMe.value = emptyList()
    }

    // ---- Commands (Cloud Functions) ----------------------------------------

    // viewModels/chain/ChainManager.kt

    fun createChain(
        title: String?,
        targetLength: Int,
        firstAssigneeUid: String,
        hiddenHistory: Boolean = false,
        ttlHours: Int = 72,
        firstMessage: String,
        authorName: String?,
        onSuccess: (String) -> Unit = {},
        onError: (Throwable) -> Unit = {  }
    ) {
        scope.launch {
            try {
                val id = userRepository.createChain(
                    title, targetLength, firstAssigneeUid, hiddenHistory, ttlHours,
                    firstMessage, authorName                         // ← pass through
                )
                onSuccess(id)
            } catch (t: Throwable) { onError(t) }
        }
    }

    fun contributeAndPass(
        chainId: String,
        text: String,
        nextAssigneeUid: String?,
        authorName: String?,                     // ← NEW
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {  }
    ) {
        scope.launch {
            try {
                userRepository.contributeAndPass(chainId, text, nextAssigneeUid, authorName) // ← pass through
                onSuccess()
            } catch (t: Throwable) { onError(t) }
        }
    }

    fun rerouteAssignee(
        chainId: String,
        newAssigneeUid: String,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = { e -> scope.launch { _events.emit(e.message ?: "reroute failed") } }
    ) {
        scope.launch {
            try {
                userRepository.rerouteAssignee(chainId, newAssigneeUid)
                onSuccess()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    // ---- Detail helpers (read-only) ----------------------------------------

    fun observeChain(chainId: String): Flow<Chain?> =
        userRepository.observeChain(chainId)

    fun observeSteps(chainId: String): Flow<List<ChainStep>> =
        userRepository.observeSteps(chainId)

    /** Pair<Chain, Steps> or null if chain doc missing */
    fun observeChainAndSteps(chainId: String): Flow<Pair<Chain, List<ChainStep>>?> =
        userRepository.observeChainAndSteps(chainId)

    // ✅ Use the "...Once" passthroughs we added to UserRepository
    suspend fun getChainOnce(chainId: String): Chain? =
        userRepository.getChainOnce(chainId)

    suspend fun getStepsOnce(chainId: String): List<ChainStep> =
        userRepository.getStepsOnce(chainId)

    // Save a chain locally by fetching current cloud state
    // Stream locally saved chains for the UI
    fun observeSavedChains(): Flow<List<SavedChainEntity>> =
        userRepository.observeSavedChains()

    // Build and store a single flat record (title, full message, contributors, created day, savedAt)
    // ChainManager.kt
    fun saveChainLocally(
        chain: Chain,
        steps: List<ChainStep>,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = { e -> scope.launch { _events.emit(e.message ?: "save failed") } }
    ) {
        scope.launch {
            try {
                // 1) Full message: title (if present) + all step texts line-by-line
                val fullMessage = buildList {
                    val t = chain.title?.trim()
                    if (!t.isNullOrEmpty()) add(t)
                    steps.forEach { add(it.text) }
                }.joinToString("\n")

                // 2) Contributors: prefer authorName, fallback to authorUid; dedup in order
                val contributorsCsv = steps
                    .map { s -> (s.authorName?.takeIf { it.isNotBlank() } ?: s.authorUid).trim() }
                    .distinct()
                    .joinToString(",")

                // 3) Day string from chain.createdAt
                val createdDate = chain.createdAt?.toDate() ?: java.util.Date()
                val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(createdDate)

                val entity = SavedChainEntity(
                    chainId = chain.id,
                    title = chain.title,
                    message = fullMessage,
                    contributorsCsv = contributorsCsv,
                    createdDay = day,
                    savedAt = System.currentTimeMillis()
                )

                userRepository.saveChainLocally(entity)
                onSuccess()
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    fun deleteSavedChain(
        chainId: String,
        onError: (Throwable) -> Unit = { e -> scope.launch { _events.emit(e.message ?: "delete failed") } }
    ) {
        scope.launch {
            try {
                userRepository.deleteSavedChain(chainId)
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

}
