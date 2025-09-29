package com.example.onlyone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAds {
    @Volatile private var rewarded: RewardedAd? = null

    // Google TEST rewarded unit (safe for dev)
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    fun preload(app: Application, nonPersonalized: Boolean = false) {
        val builder = AdRequest.Builder()
        if (nonPersonalized) {
            val extras = Bundle().apply { putString("npa", "1") }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        RewardedAd.load(
            app,
            TEST_REWARDED_ID,
            builder.build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    Log.w("Ads123", "Showing ads mf!")
                }
                override fun onAdFailedToLoad(err: LoadAdError) {
                    rewarded = null
                    Log.w("Ads123", "Rewarded load failed: ${err.message}")
                }
            }
        )
    }

    fun show(
        activity: Activity,
        onReward: (RewardItem) -> Unit = {},
        onClosed: () -> Unit = {}
    ) {
        val ad = rewarded
        if (ad == null) {
            Log.w("RewardedAds", "❌ show() called but no ad is loaded")
            onClosed()
            return
        }

        Log.d("RewardedAds", "🎬 Showing rewarded ad...")

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d("RewardedAds", "✅ Ad is now visible")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("RewardedAds", "ℹ️ Ad dismissed by user")
                rewarded = null
                preload(activity.application) // queue next
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.e(
                    "RewardedAds",
                    "❌ Failed to show: code=${e.code}, domain=${e.domain}, message=${e.message}"
                )
                rewarded = null
                preload(activity.application)
                onClosed()
            }

            override fun onAdImpression() {
                Log.d("RewardedAds", "👀 Impression recorded")
            }

            override fun onAdClicked() {
                Log.d("RewardedAds", "🖱️ Ad clicked")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(
                "RewardedAds",
                "🏆 User earned reward: type=${rewardItem.type}, amount=${rewardItem.amount}"
            )
            onReward(rewardItem)
        }
    }

}
