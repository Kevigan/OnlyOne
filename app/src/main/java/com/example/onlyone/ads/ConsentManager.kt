// com/example/onlyone/privacy/ConsentManager.kt
package com.example.onlyone.privacy

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.onlyone.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val info: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    /**
     * Call on each app launch (from an Activity).
     * In DEBUG we force EEA geography so the form shows even outside EEA.
     */
    fun requestAndShowIfRequired(
        activity: Activity,
        onFinished: (canRequestAds: Boolean, error: String?) -> Unit
    ) {
        // DEBUG: wipe cached consent so form shows on every launch
        /*if (BuildConfig.DEBUG) {
            resetForTesting()
        }*/
        // --- per-process guard to avoid duplicate popup on one launch ---
        if (askedThisSession) {
            onFinished(info.canRequestAds(), null)
            return
        }
        askedThisSession = true

        val paramsBuilder = ConsentRequestParameters.Builder()

        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // .addTestDeviceHashedId("YOUR_HASH") // optional but recommended for stable tests
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val params = paramsBuilder.build()

        Log.d("UMP", "requestConsentInfoUpdate() – starting")
        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                val status = info.consentStatus
                Log.d(
                    "UMP",
                    "requestConsentInfoUpdate() – success, status=$status, canRequestAds=${info.canRequestAds()}, privacyOptions=${info.privacyOptionsRequirementStatus}"
                )

                // Only attempt to show the form if the SDK says it's REQUIRED.
                if (status == ConsentInformation.ConsentStatus.REQUIRED) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.w("UMP", "loadAndShowConsentFormIfRequired() – error: ${formError.message}")
                        } else {
                            Log.d("UMP", "loadAndShowConsentFormIfRequired() – shown")
                        }
                        onFinished(info.canRequestAds(), formError?.message)
                    }
                } else {
                    // NOT_REQUIRED or OBTAINED (or UNKNOWN after refresh) → no popup
                    onFinished(info.canRequestAds(), null)
                }
            },
            { err ->
                Log.e("UMP", "requestConsentInfoUpdate() – failure: ${err.message}")
                onFinished(info.canRequestAds(), err.message)
            }
        )
    }

    @Volatile private var askedThisSession = false


    fun isPrivacyOptionsRequired(): Boolean =
        info.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** Used from Settings to re-open the consent options (requires Activity). */
    fun showPrivacyOptions(
        activity: Activity,
        onClosed: (error: String?) -> Unit
    ) {
        Log.d("UMP", "showPrivacyOptionsForm()")
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w("UMP", "showPrivacyOptionsForm() – error: ${formError.message}")
            }
            onClosed(formError?.message)
        }
    }

    fun canRequestAds(): Boolean = info.canRequestAds()

    /** DEBUG-ONLY: resets cached consent so you can test again. Call before request… */
    fun resetForTesting() {
        if (BuildConfig.DEBUG) {
            Log.d("UMP", "resetForTesting() – clearing local consent state")
            info.reset()
        }
    }
}
