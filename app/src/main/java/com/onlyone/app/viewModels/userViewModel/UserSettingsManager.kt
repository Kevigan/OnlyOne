package com.onlyone.app.viewModels.userViewModel

import com.onlyone.app.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserSettingsManager(
    private val userRepository: UserRepository,
    private val viewModelScope: CoroutineScope,
    private val getUid: () -> String?
) {

    fun updateNotificationPreference(
        key: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val uid = getUid() ?: return
        userRepository.updateNotificationSetting(uid, key, enabled, onSuccess, onFailure)
    }

    fun saveAppLanguage(code: String) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            try {
                userRepository.saveAppLanguage(uid, code)
            } catch (_: Throwable) {
                // optional: log/telemetry
            }
        }
    }
    fun getAppLanguage(onResult: (String?) -> Unit) {
        val uid = getUid()
        viewModelScope.launch {
            val value: String? = if (uid == null) {
                null
            } else {
                try {
                    // suspend fun, likely on IO
                    userRepository.getAppLanguage(uid) // may return null or "en"/"de"/...
                } catch (t: Throwable) {
                    null
                }
            }

            // Always callback on the main thread to avoid Compose state races
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(value)
            }
        }
    }
    fun saveNotificationToggles(
        message: Boolean,
        feedback: Boolean,
        friendRequests: Boolean   // 🆕 new toggle
    ) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            userRepository.saveLocalNotificationSettings(
                uid = uid,
                msg = message,
                feedback = feedback,
                friendRequests = friendRequests   // 🆕 forward new param
            )
        }
    }

    fun getNotificationToggles(
        onResult: (Boolean, Boolean, Boolean) -> Unit
    ) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            val (msg, fb, fr) = userRepository.getLocalNotificationSettings(uid)
            onResult(msg, fb, fr)
        }
    }
    fun saveSearchUserLanguage(lang: String) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            userRepository.saveSearchUserLanguage(uid, lang)
        }
    }

    fun getSearchUserLanguage(onResult: (String) -> Unit) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            val lang = userRepository.getSearchUserLanguage(uid)
            onResult(lang)
        }
    }

}
