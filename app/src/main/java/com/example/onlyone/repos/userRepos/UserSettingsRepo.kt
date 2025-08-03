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
        val existing = userSettingsDao.getSettings(uid)
        val updated = existing?.copy(language = language)
            ?: LocalUserSettings(uid = uid, language = language)
        userSettingsDao.saveSettings(updated)
    }

    suspend fun getAppLanguage(uid: String): String {
        return userSettingsDao.getSettings(uid)?.language ?: "en"
    }

    suspend fun saveSearchUserLanguage(uid: String, lang: String) {
        val existing = userSettingsDao.getSettings(uid)
        val updated = existing?.copy(searchUserLanguage = lang)
            ?: LocalUserSettings(uid = uid, searchUserLanguage = lang)
        userSettingsDao.saveSettings(updated)
    }

    suspend fun getSearchUserLanguage(uid: String): String {
        return userSettingsDao.getSettings(uid)?.searchUserLanguage ?: "any"
    }

    suspend fun getLocalNotificationSettings(uid: String): Pair<Boolean, Boolean> {
        val settings = userSettingsDao.getSettings(uid)
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
        val existing = userSettingsDao.getSettings(uid)
        val updated = existing?.copy(
            notifyMessages = message,
            notifyFeedback = feedback
        ) ?: LocalUserSettings(
            uid = uid,
            notifyMessages = message,
            notifyFeedback = feedback
        )
        userSettingsDao.saveSettings(updated)
    }
}
