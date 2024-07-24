package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class CollapsibleBanner {

    private var bannerView: AdView? = null

    private fun loadCollapsibleBanner(
        activity: Activity,
        frameLayout: FrameLayout,
    ) {
        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {

            adUnitId = activity.applicationContext.getString(R.string.banner)
            setAdSize(getAdSize(activity, frameLayout))

            val adRequest =
                AdRequest
                    .Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                        putString("collapsible", "bottom")
                    })
                    .build()

            loadAd(adRequest)

            adListener = object : AdListener() {
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdImpression() {}
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    frameLayout.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    displayBannerAd(frameLayout)
                }
            }
        }
    }

    fun showCollapsibleBannerAd(
        activity: Activity,
        frameLayout: FrameLayout,
        loadNewAd: Boolean
    ) {
        Log.e("TAGBannerView", "showAdaptiveBannerAd: $bannerView")
        bannerView?.let {
            displayBannerAd(frameLayout)
            if (loadNewAd) loadCollapsibleBanner(
                activity,
                frameLayout
            )

        } ?: run {
            loadCollapsibleBanner(
                activity,
                frameLayout
            )
        }
    }

    private fun displayBannerAd(adsPlaceHolder: FrameLayout) {
        try {
            if (bannerView != null) {
                val viewGroup: ViewGroup? = bannerView?.parent as? ViewGroup?
                viewGroup?.removeView(bannerView)

                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.addView(bannerView)
            } else {
                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.visibility = View.GONE
            }
        } catch (ex: Exception) {
            Log.e("AdsInformation", "inflateBannerAd: ${ex.message}")
        }

    }

    fun onResume() {
        bannerView?.resume()
    }

    fun onPause() {
        bannerView?.pause()
    }


    @Suppress("DEPRECATION")
    @Throws(Exception::class)
    private fun getAdSize(mActivity: Activity, adContainer: FrameLayout): AdSize {
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
    }
}