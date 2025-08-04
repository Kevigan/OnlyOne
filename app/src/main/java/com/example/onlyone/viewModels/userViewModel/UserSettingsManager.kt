package com.example.onlyone.viewModels.userViewModel

import android.util.Log
import com.example.onlyone.repos.userRepos.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    fun saveAppLanguage(language: String) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            userRepository.saveAppLanguage(uid, language)
        }
    }

    fun getAppLanguage(onResult: (String) -> Unit) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            val lang = userRepository.getAppLanguage(uid)
            onResult(lang)
        }
    }

    fun saveNotificationToggles(message: Boolean, feedback: Boolean) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            userRepository.saveLocalNotificationSettings(uid, message, feedback)
        }
    }

    fun getNotificationToggles(onResult: (Boolean, Boolean) -> Unit) {
        val uid = getUid() ?: return
        viewModelScope.launch {
            val result = userRepository.getLocalNotificationSettings(uid)
            onResult(result.first, result.second)
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
