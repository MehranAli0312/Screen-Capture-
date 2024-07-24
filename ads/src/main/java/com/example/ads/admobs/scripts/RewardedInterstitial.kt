package com.example.ads.admobs.scripts

import android.app.Activity
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.R
import com.example.ads.admobs.utils.loadInterstitial
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class RewardedInterstitial {

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var TAG = "MEHRAN_REWARDED_INTERSTITIAL"
    private var isGranted = false

    fun loadRewardedInterstitial(
        activity: Activity,
        adLoaded: () -> Unit,
        failedAction: () -> Unit
    ) {
        if (rewardedInterstitialAd == null) {
            RewardedInterstitialAd.load(activity.applicationContext,
                activity.getString(R.string.rewarded_interstitial),
                AdRequest.Builder().build(),

                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        rewardedInterstitialAd = ad
                        adLoaded.invoke()
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.toString())
                        rewardedInterstitialAd = null
                        failedAction.invoke()
                    }
                })
        }
    }

    fun showRewardedInterstitial(
        activity: Activity,
        rewardGrantedAction: () -> Unit,
        failedAction: () -> Unit
    ) {
        rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                if (isGranted) rewardGrantedAction.invoke()
                loadRewardedInterstitial(activity, {}, { activity.loadInterstitial({}, {}) })
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                OTHER_AD_ON_DISPLAY = false
                rewardedInterstitialAd = null
                loadRewardedInterstitial(activity, {}, { activity.loadInterstitial({}, {}) })
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                OTHER_AD_ON_DISPLAY = true
            }
        }

        rewardedInterstitialAd?.let { ad ->
            ad.show(activity) {
                isGranted = true
            }
        } ?: run {
            failedAction.invoke()
        }
    }
}