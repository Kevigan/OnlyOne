package com.onlyone.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.onlyone.app.dao.UserSettingsDao
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class OnlyOneApp : Application() {

    @Inject lateinit var userSettingsDao: UserSettingsDao

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Load saved language synchronously and apply IMMEDIATELY (no debounce!)
        val saved = runBlocking(Dispatchers.IO) { userSettingsDao.getLanguage() } ?: "de"
        val code = saved.trim().lowercase(Locale.ROOT)
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (code.isNotEmpty() && code != current) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        }
    }
}
