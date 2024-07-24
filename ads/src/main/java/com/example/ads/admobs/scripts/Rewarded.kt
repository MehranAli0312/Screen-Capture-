package com.example.ads.admobs.scripts

import android.app.Activity
import android.util.Log
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class Rewarded {

    private var rewardedAd: RewardedAd? = null
    private var isGranted = false

    private val TAG = "Mehran_Rewarded"

    fun loadRewarded(activity: Activity, adLoaded: () -> Unit, failedAction: () -> Unit) {
        if (rewardedAd == null) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity.applicationContext,
                activity.getString(R.string.rewarded),
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "onAdFailedToLoad: ")
                        rewardedAd = null
                        failedAction.invoke()
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.e(TAG, "onAdLoaded: ")
                        rewardedAd = ad
                        adLoaded.invoke()
                    }
                })
        }
    }

    fun showRewarded(
        activity: Activity,
        rewardGrantedAction: () -> Unit,
        failedAction: () -> Unit
    ) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {}

            override fun onAdDismissedFullScreenContent() {
                Log.e(TAG, "onAdDismissedFullScreenContent: $isGranted")
                OTHER_AD_ON_DISPLAY = false
                rewardedAd = null
                if (isGranted) rewardGrantedAction.invoke() else failedAction.invoke()
//                loadRewarded(activity, {}, { activity.loadInterstitial({}, {}) })
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "onAdFailedToShowFullScreenContent: ")
                OTHER_AD_ON_DISPLAY = false
                rewardedAd = null
                failedAction.invoke()
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {
                Log.e(TAG, "onAdShowedFullScreenContent: ")
                OTHER_AD_ON_DISPLAY = true
            }
        }

        rewardedAd?.let { ad ->
            ad.show(activity) {
                isGranted = true
            }
        } ?: run {
            failedAction.invoke()
        }
    }
}