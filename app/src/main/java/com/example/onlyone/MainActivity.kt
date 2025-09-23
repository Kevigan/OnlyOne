package com.example.onlyone

import android.os.Bundle
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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ❌ remove the old one-shot call
        // ConsentAndAds.showConsentThenInitAds(this, application)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // ✅ create once and provide to the whole app
            val consentManager = remember { ConsentManager(applicationContext) }

            CompositionLocalProvider(LocalConsentManager provides consentManager) {
                OnlyOneTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Navigation() // no extra params needed
                        }
                    }
                }
            }
        }
    }
}


