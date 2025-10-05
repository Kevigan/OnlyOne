package com.example.onlyone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAds {
    @Volatile private var rewarded: RewardedAd? = null

    // ✅ Your real Rewarded ad unit
    private const val PROD_REWARDED_ID = "ca-app-pub-7925168977173280/7684779513"
    // ✅ Google test Rewarded ad unit
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private fun rewardedId(): String =
        if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    fun preload(app: Application, nonPersonalized: Boolean = false) {
        val builder = AdRequest.Builder()
        if (nonPersonalized) {
            val extras = Bundle().apply {
                putString("npa", "1")    // Non-personalized ads
                putInt("gad_rdp", 1)     // (Optional) Restrict data processing
            }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        RewardedAd.load(
            app,
            rewardedId(),
            builder.build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    Log.d("RewardedAds", "✅ Rewarded loaded (id=${rewardedId()})")
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    rewarded = null
                    val ri = err.responseInfo
                    val adapterDetails = ri?.adapterResponses?.joinToString("\n") { ar ->
                        val e = ar.adError
                        "- ${ar.adapterClassName}: latency=${ar.latencyMillis}ms, " +
                                "error='${e?.message}' code=${e?.code} domain=${e?.domain}"
                    }
                    Log.w(
                        "RewardedAds",
                        """
                        ❌ Load failed:
                        code=${err.code}, domain=${err.domain}, msg=${err.message}
                        mediationAdapter=${ri?.mediationAdapterClassName}
                        responseId=${ri?.responseId}
                        Adapter responses:
                        $adapterDetails
                        """.trimIndent()
                    )
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
            Log.w("RewardedAds", "⚠️ show() called but no ad is loaded; preloading now")
            preload(activity.application)
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
