package com.onlyone.app.repos

import com.onlyone.app.dao.UserSettingsDao
import com.onlyone.app.data.LocalUserSettings
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
        return userSettingsDao.getSettings()?.language ?: "de"
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

    suspend fun getLocalNotificationSettings(uid: String): Triple<Boolean, Boolean, Boolean> {
        val settings = userSettingsDao.getSettings()
        return Triple(
            settings?.notifyMessages ?: true,        // 🔔 message notifications
            settings?.notifyFeedback ?: true,        // 🔔 feedback notifications
            settings?.notifyFriendRequests ?: true   // 🧩 friend request notifications (NEW)
        )
    }

    suspend fun saveLocalNotificationSettings(
        uid: String,
        message: Boolean,
        feedback: Boolean,
        friendRequests: Boolean    // 🆕 add parameter for friend request toggle
    ) {
        val existing = userSettingsDao.getSettings()
        val updated = existing?.copy(
            notifyMessages = message,
            notifyFeedback = feedback,
            notifyFriendRequests = friendRequests   // 🆕 include new field
        ) ?: LocalUserSettings(
            notifyMessages = message,
            notifyFeedback = feedback,
            notifyFriendRequests = friendRequests   // 🆕 include in default
        )
        userSettingsDao.saveSettings(updated)
    }

}
