package com.example.ads.admobs.scripts

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.ads.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class Native {
    private var nativeAd: NativeAd? = null


    fun destroyMyNative() {
        nativeAd?.destroy()
    }

    fun loadAndShowNative(
        activity: Activity,
        nativeAdLayout: Int,
        adId: Int,
        loadedAction: (nativeAdView: NativeAdView?) -> Unit,
        failedAction: () -> Unit
    ) {
        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(
            activity,
            activity.applicationContext.getString(adId)
        )
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
                populateNativeAdView(activity, nativeAdLayout, ad, loadedAction, failedAction)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failedAction.invoke()
                }

            })
            .withNativeAdOptions(
                adOptions
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadNative(
        activity: Activity,
        loadedAction: (nativeAd: NativeAd?) -> Unit,
        failedAction: () -> Unit
    ) {
        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        val adLoader = AdLoader.Builder(
            activity,
            activity.applicationContext.getString(R.string.native_advanced_video)
        )
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
                loadedAction.invoke(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    failedAction.invoke()
                }
            })
            .withNativeAdOptions(
                adOptions
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun populateNativeAdView(
        activity: Activity,
        nativeAdLayout: Int,
        nativeAd: NativeAd?,
        loadedAction: (nativeAdView: NativeAdView?) -> Unit,
        failedAction: () -> Unit
    ) {
        nativeAd?.let {
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val adView = try {
                inflater.inflate(nativeAdLayout, null) as NativeAdView
            } catch (ex: ClassCastException) {
                return
            }
            // Set the media view.
            adView.mediaView = adView.findViewById(R.id.ad_media)

            // Set other ad assets.
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_app_icon)
            adView.priceView = adView.findViewById(R.id.ad_price)
            adView.starRatingView = adView.findViewById(R.id.ad_stars)
            adView.storeView = adView.findViewById(R.id.ad_store)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

            // The headline and media content are guaranteed to be in every UnifiedNativeAd.
            (adView.headlineView as TextView).text = nativeAd.headline
            nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
            // check before trying to display them.
            if (nativeAd.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }

            if (nativeAd.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as Button).text = nativeAd.callToAction
            }

            if (nativeAd.icon == null) {
                adView.iconView?.visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.icon?.drawable
                )
                adView.iconView?.visibility = View.VISIBLE
            }

            if (nativeAd.price == null) {
                adView.priceView?.visibility = View.INVISIBLE
            } else {
                adView.priceView?.visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }

            if (nativeAd.store == null) {
                adView.storeView?.visibility = View.INVISIBLE
            } else {
                adView.storeView?.visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }

            if (nativeAd.starRating == null) {
                adView.starRatingView?.visibility = View.INVISIBLE
            } else {
                (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                adView.starRatingView?.visibility = View.VISIBLE
            }

            if (nativeAd.advertiser == null) {
                adView.advertiserView?.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView?.visibility = View.VISIBLE
            }

            adView.setNativeAd(nativeAd)
            loadedAction.invoke(adView)
        } ?: run {
            failedAction.invoke()
        }
    }
}