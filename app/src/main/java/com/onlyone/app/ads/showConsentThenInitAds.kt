// ConsentAndAds.kt
package com.onlyone.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.ump.*

object ConsentAndAds {

    fun showConsentThenInitAds(activity: Activity, app: Application) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                // ✅ This API REQUIRES an Activity
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initAdsAndPreload(app, consentInfo)
                }
            },
            { e ->
                Log.w("UMP", "Consent info update failed: ${e.message}")
                // Fail-open for dev; proceed with NPA
                initAdsAndPreload(app, consentInfo)
            }
        )
    }

    private fun initAdsAndPreload(app: Application, consentInfo: ConsentInformation) {
        // Mark your device as test during development (replace after Logcat shows it)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("YOUR_HASHED_TEST_DEVICE_ID"))
                .build()
        )

        MobileAds.initialize(app) {}

        val nonPersonalized = consentInfo.consentStatus != ConsentInformation.ConsentStatus.OBTAINED
        Ads.preloadInterstitial(app, nonPersonalized)
        RewardedAds.preload(app, nonPersonalized)
    }
}

/** Same Ads helper you had, kept here for convenience. */
object Ads {
    @Volatile private var interstitial: InterstitialAd? = null
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    fun preloadInterstitial(app: Application, nonPersonalized: Boolean = false) {
        val reqBuilder = AdRequest.Builder()
        if (nonPersonalized) {
            val extras = Bundle().apply { putString("npa", "1") }
            reqBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        InterstitialAd.load(
            app,
            TEST_INTERSTITIAL_ID,
            reqBuilder.build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    Log.w("Ads", "Load fail: ${error.message}")
                }
            }
        )
    }

    fun show(activity: Activity, onClosed: () -> Unit = {}) {
        val ad = interstitial ?: return onClosed()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preloadInterstitial(activity.application)
                onClosed()
            }
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                interstitial = null
                preloadInterstitial(activity.application)
                onClosed()
            }
        }
        ad.show(activity)
    }
}
