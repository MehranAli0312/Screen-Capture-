package com.example.janbarktask.utills

import android.app.Activity
import android.content.Context
import android.content.Intent

class ScreenCaptureUtil(private val context: Context) {
    private val prefKey = "ScreenCapture"
    private val resultIntKey = "resultIntKey"
    private val resultStringKey = "resultStringKey"

    fun getScreenCapture(): Pair<Int, Intent?> {
        context.getSharedPreferences(prefKey, Context.MODE_PRIVATE).apply {
            val resultCode = getInt(resultIntKey, Activity.RESULT_CANCELED)
            val dataString = getString(resultStringKey, null)
            val data = dataString?.let { Intent.parseUri(it, Intent.URI_INTENT_SCHEME) }
            return Pair(resultCode, data)
        }
    }

    fun setScreenCapture(resultCode: Int, data: Intent) {
        context.getSharedPreferences(prefKey, Context.MODE_PRIVATE)
            .edit().apply {
                putInt(resultIntKey, resultCode)
                putString(resultStringKey, data.toUri(Intent.URI_INTENT_SCHEME))
                    .apply()
            }
    }

}