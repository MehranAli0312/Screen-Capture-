package com.example.janbarktask.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.janbarktask.R
import com.example.janbarktask.enums.Actions
import com.example.janbarktask.receivers.ActionReceiver
import com.example.janbarktask.viewModels.GalleryViewModel
import org.koin.android.ext.android.inject


class MyForegroundService : Service() {

    private var mediaProjection: MediaProjection? = null

    private val viewModel: GalleryViewModel by inject()

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action?.let { Actions.valueOf(it) } ?: Actions.START

        when (action) {
            Actions.STOP -> {
                stopSelf()
            }

            Actions.SCREEN_SHOT -> viewModel.takeScreenshot(mediaProjection)
            Actions.START -> {
                val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
                val data = intent?.getParcelableExtra<Intent>("data")
                if (resultCode != Activity.RESULT_CANCELED && data != null) {
                    val mediaProjectionManager =
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection =
                        resultCode?.let { mediaProjectionManager.getMediaProjection(it, data) }
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MY_CHANNEL_ID",
                "My Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, ActionReceiver::class.java).apply {
            action = Actions.STOP.name
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val screenshotIntent = Intent(this, ActionReceiver::class.java).apply {
            action = Actions.SCREEN_SHOT.name
        }
        val screenshotPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            screenshotIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "MY_CHANNEL_ID")
            .setContentTitle("Foreground Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_bell)
            .addAction(R.drawable.ic_cross, "Stop Service", stopPendingIntent)
            .addAction(R.drawable.ic_screen_shot, "Take Screenshot", screenshotPendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        stopForeground(true)
    }
}
