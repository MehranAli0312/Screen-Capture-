package com.example.janbarktask.ui.activities

import android.os.Bundle
import com.example.ads.admobs.utils.MyMobileAds
import com.example.ads.admobs.utils.loadInterstitial
import com.example.janbarktask.R

class MainActivity : Permissions() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyMobileAds().myInitialize(this) {
            loadInterstitial({}, {})
        }
    }

}