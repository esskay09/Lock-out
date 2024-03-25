package com.terrranullius.lockout.other

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.terrranullius.lockout.R
import com.terrranullius.lockout.di.LockoutServiceModule
import com.terrranullius.lockout.other.Constants.NOTIFICATION_CHANNEL_ID
import com.terrranullius.lockout.ui.MainActivity

fun Context.setNotificationChannel(context: Context) {
    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Stay Focused",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }
}

fun Context.sendNotification(
    contentTitle: String = "",
) {
    val notifBuilder = LockoutServiceModule.providesBaseNotificationBuilder(this)
        .setFullScreenIntent(
            PendingIntent.getActivity(
                this,
                101,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ), true
        )

    if (contentTitle.isNotEmpty()) notifBuilder.setContentTitle(contentTitle)

    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Constants.NOTIFICATION_ID, notifBuilder.build())
}

fun Context.sendNotificationCompleted(contentTitle: String) {
    val notif = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(contentTitle)
        .setSmallIcon(R.drawable.ic_baseline_lock_24)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setOngoing(false)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 458,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setVibrate(longArrayOf(50, 50, 100, 50, 100))
        .build()

    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.notify(Constants.NOTIFICATION_ID, notif)
}