package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.ads.Constants
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.appIsForeground
import com.example.ads.myExtensios.isProVersion
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpen {

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    val LOG_TAG = "MEHRAN_APP_OPEN"
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false


    var isShowingAd = false

    fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable() || isProVersion()) {
            return
        }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        if (appIsForeground) {
            AppOpenAd.load(
                context, context.getString(com.example.ads.R.string.app_open), request,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG_TAG, "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false
                    }
                })
        }
    }

    /** Shows the ad if one isn't already showing. */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        if (OTHER_AD_ON_DISPLAY) {
            Log.d(LOG_TAG, "The other ad is already showing.")
            return
        }

        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            com.example.ads.Constants.appOpenStarted = false
            onShowAdCompleteListener.onShowAdComplete()
//            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                appOpenAd = null
                isShowingAd = false
                com.example.ads.Constants.appOpenStarted = false
                onShowAdCompleteListener.onShowAdComplete()
//                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, adError.message)
                OTHER_AD_ON_DISPLAY = false
                appOpenAd = null
                isShowingAd = false
                com.example.ads.Constants.appOpenStarted = false
                onShowAdCompleteListener.onShowAdComplete()
//                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = true
                com.example.ads.Constants.appOpenStarted = true
                Log.d(LOG_TAG, "Ad showed fullscreen content.")
            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
}
