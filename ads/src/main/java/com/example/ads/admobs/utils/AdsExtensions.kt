package com.example.ads.admobs.utils

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ads.Constants
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.example.ads.Constants.appOpen
import com.example.ads.Constants.OTHER_AD_ON_DISPLAY
import com.example.ads.Constants.banner
import com.example.ads.Constants.clickCount
import com.example.ads.Constants.collapsibleBanner
import com.example.ads.Constants.inlineBanner
import com.example.ads.Constants.interstitial
import com.example.ads.Constants.isInterstitialShowed
import com.example.ads.Constants.native
import com.example.ads.Constants.rewarded
import com.example.ads.Constants.rewardedInterstitial
import com.example.ads.R
import com.example.ads.admobs.scripts.AppOpen
import com.example.ads.myExtensios.hide
import com.example.ads.myExtensios.isNetworkAvailable
import com.example.ads.myExtensios.isProVersion
import com.example.ads.myExtensios.show
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private val TAG = "TAG"
fun Activity?.loadAppOpen() {
    this?.let {
        Log.e(TAG, "loadAppOpen: ${Constants.appIsForeground}")
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) appOpen.loadAd(this.applicationContext)
            else {
                MyMobileAds().myInitialize(this.applicationContext) {
                    ADS_SDK_INITIALIZE = true
                    appOpen.loadAd(this.applicationContext)
                }
            }
        }
    }
}

fun Activity?.showAppOpen(onCompleteAction: () -> Unit) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            appOpen.showAdIfAvailable(this, object : AppOpen.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    onCompleteAction.invoke()
                }
            })
        } else onCompleteAction.invoke()
    } ?: run {
        onCompleteAction.invoke()
    }
}

fun appOpenLoaded() = appOpen.isAdAvailable()


fun Activity?.showAdaptiveBanner(
    container: ConstraintLayout,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            if (ADS_SDK_INITIALIZE) banner.showAdaptiveBannerAd(
                this, container, frameLayout, shimmerFrameLayout, loadNewAd
            )
            else {
                MyMobileAds().myInitialize(application) { ADS_SDK_INITIALIZE = true }
                frameLayout.hide()
                shimmerFrameLayout.show()
            }
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    } ?: run {
        container.hide()
        frameLayout.hide()
        shimmerFrameLayout.hide()
    }
}

fun Activity?.onResumeBanner(
    container: ConstraintLayout,
    frameLayout: FrameLayout,
    shimmerFrameLayout: ShimmerFrameLayout,
    loadNewAd: Boolean = true
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            banner.onResume()
            showAdaptiveBanner(container, frameLayout, shimmerFrameLayout, loadNewAd)
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    }
}

fun onPauseBanner() {
    if (!isProVersion()) banner.onPause()
}

fun Activity?.showCollapsibleBanner(
    frameLayout: FrameLayout,
    loadNewAd: Boolean
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            if (ADS_SDK_INITIALIZE) collapsibleBanner.showCollapsibleBannerAd(
                this, frameLayout, loadNewAd
            )
            else {
                MyMobileAds().myInitialize(application) { ADS_SDK_INITIALIZE = true }
                frameLayout.hide()
            }
        } else {
            frameLayout.hide()
        }
    } ?: run {
        frameLayout.hide()
    }
}

fun Activity?.onResumeCollapsibleBanner(
    frameLayout: FrameLayout,
    loadNewAd: Boolean = true
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            collapsibleBanner.onResume()
            showCollapsibleBanner(frameLayout, loadNewAd)
        } else {
            frameLayout.hide()
        }
    }
}

fun onPauseCollapsibleBanner() {
    if (!isProVersion()) collapsibleBanner.onPause()
}


fun Activity?.showAdaptiveInlineBanner(
    container: ConstraintLayout, frameLayout: FrameLayout, shimmerFrameLayout: ShimmerFrameLayout
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            if (ADS_SDK_INITIALIZE) inlineBanner.showAdaptiveInlineBannerAd(
                this, container, frameLayout, shimmerFrameLayout, true
            )
            else {
                MyMobileAds().myInitialize(application) { ADS_SDK_INITIALIZE = true }
                frameLayout.hide()
                shimmerFrameLayout.show()
            }
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    } ?: run {
        container.hide()
        frameLayout.hide()
        shimmerFrameLayout.hide()
    }
}

fun Activity?.onResumeInlineBanner(
    container: ConstraintLayout, frameLayout: FrameLayout, shimmerFrameLayout: ShimmerFrameLayout
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion()) {
            inlineBanner.onResume()
            showAdaptiveInlineBanner(container, frameLayout, shimmerFrameLayout)
        } else {
            container.hide()
            frameLayout.hide()
            shimmerFrameLayout.hide()
        }
    }
}

fun Activity?.onPauseInlineBanner() {
    if (!isProVersion()) inlineBanner.onPause()
}

fun destroyNative() {
    native.destroyMyNative()
}

fun Activity?.loadAndShowNative(
    nativeAdLayout: Int,
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit,
    adId: Int = R.string.native_advanced_video
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) native.loadAndShowNative(
            this, nativeAdLayout, adId, loadedAction, failedAction
        ) else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadNative(
    loadedAction: (nativeAd: NativeAd?) -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) native.loadNative(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showNative(
    nativeAdLayout: Int,
    nativeAd: NativeAd,
    loadedAction: (nativeAdView: NativeAdView?) -> Unit,
    failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) native.populateNativeAdView(
            this, nativeAdLayout, nativeAd, loadedAction, failedAction
        )
        else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}


private var loadInterstitialAdCounter = 0
fun isOddLoad() = loadInterstitialAdCounter % 2 == 0
fun Activity?.loadInterstitialOdd(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        loadInterstitialAdCounter += 1
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground && isOddLoad() && loadInterstitialAdCounter != 1) {
            if (ADS_SDK_INITIALIZE) interstitial.loadInterstitial(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadInterstitial(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) interstitial.loadInterstitial(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun canShowInterstitialAd() = clickCount % 3 == 0

fun checkAndUpdateClickCount() {
    Log.e(
        TAG,
        "checkAndUpdateClickCount: isInterstitialShowed = $isInterstitialShowed  ,,  canShowInterstitialAd() = ${canShowInterstitialAd()} ,,  clickCount=$clickCount",
    )
    try {
        if (isInterstitialShowed || !canShowInterstitialAd()) {
            isInterstitialShowed = false
            clickCount++
        }
    } catch (_: Exception) {
    }
}

fun isInterstitialAvailable() = interstitial.isAdAvailable()

fun Activity?.showInterstitial(
    loadedAction: () -> Unit, failedAction: () -> Unit, preload: Boolean = true
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) interstitial.showInterstitial(
            this, preload, loadedAction
        ) {
            failedAction.invoke()
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}


private var interstitialAdCounter = 0
fun isOdd() = interstitialAdCounter % 2 == 0

fun Activity?.showInterstitialOdd(
    loadedAction: () -> Unit, failedAction: () -> Unit, preload: Boolean = true
) {
    this?.let {
        interstitialAdCounter += 1
        Log.e(TAG, "showInterstitialOddText: $interstitialAdCounter  ,,  ${isOdd()}")

        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && isOdd() && interstitialAdCounter != 1) interstitial.showInterstitial(
            this, preload, loadedAction, failedAction
        )
        else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

private var interstitialAdCounterText = 0
fun isOddText() = interstitialAdCounterText % 2 == 0

fun Activity?.showInterstitialOddText(
    loadedAction: () -> Unit, failedAction: () -> Unit, preload: Boolean = true
) {
    this?.let {
        interstitialAdCounterText += 1
        Log.e(TAG, "showInterstitialOddText: $interstitialAdCounterText  ,,  ${isOddText()}")
        if (canShowInterstitialAd()) {
            if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground && isOddText() && interstitialAdCounterText != 1) interstitial.showInterstitial(
                this, preload, loadedAction, failedAction
            )
            else failedAction.invoke()
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadRewarded(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) rewarded.loadRewarded(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showRewarded(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) rewarded.showRewarded(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.loadRewardedInterstitial(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) rewardedInterstitial.loadRewardedInterstitial(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}

fun Activity?.showRewardedInterstitial(
    loadedAction: () -> Unit, failedAction: () -> Unit
) {
    this?.let {
        if (isNetworkAvailable() && !isProVersion() && !OTHER_AD_ON_DISPLAY && Constants.appIsForeground) {
            if (ADS_SDK_INITIALIZE) rewardedInterstitial.showRewardedInterstitial(
                this, loadedAction, failedAction
            )
            else MyMobileAds().myInitialize(this.applicationContext) { ADS_SDK_INITIALIZE = true }
        } else failedAction.invoke()
    } ?: run {
        failedAction.invoke()
    }
}
