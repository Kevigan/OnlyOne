package com.example.onlyone

import android.app.Application
import com.example.dao.UserSettingsDao
import com.example.onlyone.utils.applyAppLocale

import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class OnlyOneApp : Application() {

    @Inject
    lateinit var userSettingsDao: UserSettingsDao

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        val lang = runBlocking(Dispatchers.IO) {
            // ensureSettingsRow(userSettingsDao) // optional
            userSettingsDao.getLanguage()
        } ?: "en"

        applyAppLocale(lang)
    }
}




