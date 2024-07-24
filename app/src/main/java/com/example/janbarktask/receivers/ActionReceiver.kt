package com.example.janbarktask.receivers

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.example.ads.myExtensios.showToast
import com.example.janbarktask.enums.Actions
import com.example.janbarktask.helper.isAccessibilityServiceEnabled
import com.example.janbarktask.services.MyAccessibilityService
import com.example.janbarktask.services.MyForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Actions.STOP.name -> {
                val stopIntent = Intent(context, MyForegroundService::class.java)
                context.stopService(stopIntent)
                exitProcess(0)
            }

            Actions.SCREEN_SHOT.name -> {
                collapseNotificationTray()

                // Delay the screenshot action
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000) // Delay for 2 seconds (2000 milliseconds)
                    Log.e("TAG000", "onReceive: tak")
                    takeScreenshot(context)
                }
            }
        }
    }

    private fun takeScreenshot(context: Context) {
        val screenshotIntent = Intent(context, MyForegroundService::class.java).apply {
            action = Actions.SCREEN_SHOT.name
        }
        context.startService(screenshotIntent)
    }

    private fun collapseNotificationTray() {
        MyAccessibilityService.instance?.collapseNotificationTray()
    }

}