package com.example.ads

import com.example.ads.admobs.scripts.AppOpen
import com.example.ads.admobs.scripts.Banner
import com.example.ads.admobs.scripts.CollapsibleBanner
import com.example.ads.admobs.scripts.InlineBanner
import com.example.ads.admobs.scripts.Interstitial
import com.example.ads.admobs.scripts.Native
import com.example.ads.admobs.scripts.Rewarded
import com.example.ads.admobs.scripts.RewardedInterstitial

object Constants {
    var clickCount = 3
    var isInterstitialShowed = false
    var ADS_SDK_INITIALIZE: Boolean = false
    var OTHER_AD_ON_DISPLAY = false

    var appOpen = AppOpen()

    var banner = Banner()
    var collapsibleBanner = CollapsibleBanner()
    var inlineBanner = InlineBanner()
    var native = Native()
    var rewarded = Rewarded()
    var rewardedInterstitial = RewardedInterstitial()
    var interstitial = Interstitial()

    var appIsForeground = true
    var appOpenStarted = false

    var isFromFeatured = false
    var isFromRewarded = false
    var isInterstitial = false

    var notFirstLaunch: Boolean = false


}