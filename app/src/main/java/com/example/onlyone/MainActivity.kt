package com.example.onlyone

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.onlyone.ads.LocalConsentManager
import com.example.onlyone.privacy.ConsentManager
import com.example.onlyone.ui.theme.OnlyOneTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) { status ->
            Log.d("AdsInit", "MobileAds initialized: $status")
        }

        // ✅ Put test device IDs behind DEBUG
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR /*, "HASHED_TEST_DEVICE_ID"*/))
                    .build()
            )
        }

        // UMP → preload
        ConsentAndAds.showConsentThenInitAds(this, application)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val consentManager = remember { ConsentManager(applicationContext) }
            CompositionLocalProvider(LocalConsentManager provides consentManager) {
                OnlyOneTheme {
                    Surface(modifier = Modifier.fillMaxSize()) { Navigation() }
                }
            }
        }
    }
}




