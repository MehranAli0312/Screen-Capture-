package com.example.janbarktask.di

import com.example.janbarktask.viewModels.GalleryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
//    viewModel { GalleryViewModel(androidContext()) }
    single { GalleryViewModel(androidContext()) }
}