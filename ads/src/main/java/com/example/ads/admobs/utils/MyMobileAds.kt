package com.example.ads.admobs.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class MyMobileAds {

    private lateinit var consentInformation: ConsentInformation

    fun myInitialize(context: Context, onCompletion: () -> Unit) {
        if (!ADS_SDK_INITIALIZE) {
            CoroutineScope(IO).launch {
                MobileAds.initialize(context) {
                    ADS_SDK_INITIALIZE = true
                    onCompletion.invoke()
                }
            }
        }
    }


    fun checkAdmobConsent(
        activity: Activity,
        onCompletion: () -> Unit,
        consentCompletionCallback: () -> Unit
    ) {
//        val debugSettings = ConsentDebugSettings.Builder(activity)
//            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//            .addTestDeviceHashedId("D5878EE519458D733C985D1ED472F0E3")
//            .build()
//
//        val params = ConsentRequestParameters
//            .Builder()
//            .setConsentDebugSettings(debugSettings)
//            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity.applicationContext)
//        consentInformation.reset()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    Log.w(
                        "Mehran_CONSENT_GDPR",
                        String.format(
                            "%s: %s",
                            loadAndShowError?.errorCode,
                            loadAndShowError?.message
                        )
                    )

                    if (consentInformation.canRequestAds()) {
                        myInitialize(activity.applicationContext) {
                            onCompletion.invoke()
                        }
                    }
                    consentCompletionCallback.invoke()
                }
            },
            { requestConsentError ->
                Log.w(
                    "Mehran_CONSENT_GDPR",
                    String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
                consentCompletionCallback.invoke()
            }
        )

        if (consentInformation.canRequestAds()) {
            myInitialize(activity.applicationContext) {
                onCompletion.invoke()
            }
        }
    }
}