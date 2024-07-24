package com.example.ads.admobs.scripts

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.R
import com.example.ads.admobs.utils.BannerType
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class Banner {

    private var bannerView: AdView? = null

    private fun loadAdaptiveBanner(
        activity: Activity,
        container: ConstraintLayout,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout, bannerType: BannerType = BannerType.ADAPTIVE_BANNER
    ) {
        bannerView = AdView(activity.applicationContext)
        bannerView?.apply {
            adUnitId = activity.applicationContext.getString(R.string.banner)
            setAdSize(adSizeBanner(activity))
            val adRequest = AdRequest.Builder().build()
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdOpened() {}
                override fun onAdImpression() {}
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    container.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                    shimmerFrameLayout.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    container.visibility = View.VISIBLE
                    frameLayout.visibility = View.VISIBLE
                    frameLayout.removeAllViews()
                    frameLayout.addView(this@apply)
                    shimmerFrameLayout.visibility = View.GONE
                }
            }
        }
    }

    fun showAdaptiveBannerAd(
        activity: Activity,
        container: ConstraintLayout,
        frameLayout: FrameLayout,
        shimmerFrameLayout: ShimmerFrameLayout,
        loadNewAd: Boolean
    ) {
        Log.e("TAGBannerView", "showAdaptiveBannerAd: $bannerView")
        bannerView?.let {

            frameLayout.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
            frameLayout.removeAllViews()
            if (it.parent != null) {
                (it.parent as ViewGroup).removeView(it)
            }
            frameLayout.addView(it)
            if (loadNewAd) loadAdaptiveBanner(
                activity,
                container,
                frameLayout,
                shimmerFrameLayout
            )

            Handler(Looper.getMainLooper()).postDelayed({
                shimmerFrameLayout.visibility = View.GONE
            }, 1000)

        } ?: run {
            loadAdaptiveBanner(
                activity,
                container,
                frameLayout,
                shimmerFrameLayout
            )
        }
    }

    fun onResume() {
        bannerView?.resume()
    }

    fun onPause() {
        bannerView?.pause()
    }

    private fun adSizeBanner(activity: Activity): AdSize {
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics =
                activity.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            bounds.width().toFloat()
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(
                displayMetrics
            )
            displayMetrics.widthPixels.toFloat()
        }
        val density = activity.applicationContext.resources.displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity.applicationContext,
            adWidth
        )
    }
}