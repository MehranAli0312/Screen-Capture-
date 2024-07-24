package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.ads.Constants
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class Interstitial {
    private var mInterstitialAd: InterstitialAd? = null

    fun isAdAvailable(): Boolean {
        return mInterstitialAd != null
    }

    private val TAG="Mehran_Interstitial"

    fun loadInterstitial(
        context: Context,
        adLoaded: () -> Unit,
        adFailed: () -> Unit
    ) {
        if (mInterstitialAd == null) {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                context.getString(R.string.interstitial),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        adFailed.invoke()
                        Log.d(TAG, adError.toString())
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                        adLoaded.invoke()
                    }
                })
        } else adLoaded.invoke()
    }

    fun showInterstitial(
        activity: Activity, preload: Boolean = true,
        loadedAction: () -> Unit,
        failedAction: () -> Unit
    ) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d("TAG", "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Constants.isInterstitialShowed = true
                OTHER_AD_ON_DISPLAY = false
                loadedAction.invoke()
                Log.d("TAG", "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                if (preload) loadInterstitial(activity, {}, {})
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                OTHER_AD_ON_DISPLAY = false
                Log.e("TAG", "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d("TAG", "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                OTHER_AD_ON_DISPLAY = true
                Log.d("TAG", "Ad showed fullscreen content.")
            }
        }
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
            loadInterstitial(activity, {}, {})
            failedAction.invoke()
        }
    }
}