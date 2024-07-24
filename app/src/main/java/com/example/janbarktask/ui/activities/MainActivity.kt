package com.example.janbarktask.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.example.ads.admobs.utils.MyMobileAds
import com.example.ads.admobs.utils.loadInterstitial
import com.example.janbarktask.R
import com.example.janbarktask.viewModels.GalleryViewModel
import org.koin.android.ext.android.inject

class MainActivity : Permissions() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyMobileAds().myInitialize(this) {
            loadInterstitial({}, {})
        }
    }

}