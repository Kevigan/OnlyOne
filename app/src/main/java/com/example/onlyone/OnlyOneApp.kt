package com.example.onlyone

import android.app.Application
import com.example.dao.UserSettingsDao
import com.example.onlyone.utils.applyAppLocale
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// --- Google Mobile Ads + UMP ---
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

// OnlyOneApp.kt
@HiltAndroidApp
class OnlyOneApp : Application() {

    @Inject lateinit var userSettingsDao: UserSettingsDao

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val lang = runBlocking(Dispatchers.IO) { userSettingsDao.getLanguage() } ?: "en"
        applyAppLocale(lang)
    }
}

