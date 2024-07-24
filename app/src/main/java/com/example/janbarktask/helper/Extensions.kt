package com.example.janbarktask.helper

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.example.ads.myExtensios.showToast
import com.example.janbarktask.R
import com.example.janbarktask.services.MyAccessibilityService
import com.example.janbarktask.utills.UnifiedNativeAd
import kotlin.time.Duration.Companion.seconds

private var lastClickTime: Long = 0

fun View.setSingleClickListener(delayTimeInSeconds: Int = 1, action: () -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastClickTime) >= delayTimeInSeconds.seconds.inWholeMilliseconds / 2) {
            lastClickTime = currentTime
            action.invoke()
        }
    }
}


fun adAdsIntoList(templateList: List<Any>): ArrayList<Any> {
    val updatedAdsList = ArrayList<Any>()
    templateList.forEachIndexed { index, any ->
        if (index != 0 && index % 6 == 0) {
            updatedAdsList.add(UnifiedNativeAd())
        }
        updatedAdsList.add(any)
    }
    return updatedAdsList
}

fun Activity.shareImage(imageUri: Uri?) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        val chooserIntent = Intent.createChooser(shareIntent, "Share Image")
        startActivity(chooserIntent)
    } catch (ex: Exception) {
        Log.d("Mehran", "shareImage: ${ex.message}")
    }
}


fun Context.isAccessibilityServiceEnabled(): Boolean {
    val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    Log.d("AccessibilityCheck", "Enabled services: $enabledServices")

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)

    val myService = ComponentName(this, MyAccessibilityService::class.java)
    val myServiceFlattened = myService.flattenToString()
    Log.d("AccessibilityCheck", "My service: $myServiceFlattened")

    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        Log.d("AccessibilityCheck", "Checking service: $componentName")
        if (componentName.equals(myServiceFlattened, ignoreCase = true)) {
            return true
        }
    }
    return false
}


fun Context.toastMessageAccessibility() {
    showToast(
        getString(
            R.string.toast_open_accessibility_settings,
            getString(R.string.app_name)
        )
    )
}
