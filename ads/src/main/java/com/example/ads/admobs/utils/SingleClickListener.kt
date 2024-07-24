package com.example.ads.admobs.utils

import android.os.SystemClock
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

class SingleClickListener(private val block: () -> Unit) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 700) {
            return
        }
        checkAndUpdateClickCount()
        lastClickTime = SystemClock.elapsedRealtime()

        block()
    }
}

fun View.setOnSingleClickListener(block: () -> Unit) {
    setOnClickListener(SingleClickListener(block))
}

fun FragmentActivity.onMyBack(onBackPressedCallback: OnBackPressedCallback) {
    onBackPressedDispatcher.addCallback(onBackPressedCallback)
}
