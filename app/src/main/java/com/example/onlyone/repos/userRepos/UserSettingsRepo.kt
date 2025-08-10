package com.example.onlyone.repos

import com.example.dao.UserSettingsDao
import com.example.onlyone.data.LocalUserSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepo @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) {
    suspend fun saveAppLanguage(uid: String, language: String) {
        val existing = userSettingsDao.getSettings()
        val updated = existing?.copy(language = language)
            ?: LocalUserSettings(language = language)
        userSettingsDao.saveSettings(updated)
    }

    suspend fun getAppLanguage(uid: String): String {
        return userSettingsDao.getSettings()?.language ?: "en"
    }

    suspend fun saveSearchUserLanguage(uid: String, lang: String) {
        val existing = userSettingsDao.getSettings()
        val updated = existing?.copy(searchUserLanguage = lang)
            ?: LocalUserSettings(searchUserLanguage = lang)
        userSettingsDao.saveSettings(updated)
    }

    suspend fun getSearchUserLanguage(uid: String): String {
        return userSettingsDao.getSettings()?.searchUserLanguage ?: "any"
    }

    suspend fun getLocalNotificationSettings(uid: String): Pair<Boolean, Boolean> {
        val settings = userSettingsDao.getSettings()
        return Pair(
            settings?.notifyMessages ?: true,
            settings?.notifyFeedback ?: true
        )
    }

    suspend fun saveLocalNotificationSettings(
        uid: String,
        message: Boolean,
        feedback: Boolean
    ) {
        val existing = userSettingsDao.getSettings()
        val updated = existing?.copy(
            notifyMessages = message,
            notifyFeedback = feedback
        ) ?: LocalUserSettings(
            notifyMessages = message,
            notifyFeedback = feedback
        )
        userSettingsDao.saveSettings(updated)
    }
}
