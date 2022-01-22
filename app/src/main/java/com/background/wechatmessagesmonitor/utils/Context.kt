package com.background.wechatmessagesmonitor.utils

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.background.wechatmessagesmonitor.R
import com.background.wechatmessagesmonitor.constants.NOTIFICATION_CHANNEL_ID

fun Context.createNotification(
    ticker: CharSequence = "啄木鸟助手",
    title: CharSequence = "啄木鸟助手运行中"
): Notification {
    return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setTicker(ticker)
        .setContentTitle(title)
        .build()
}

fun Context.startServiceCompat(cls: Class<out Service>) {
    val service = Intent(this, cls)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(service)
    } else {
        startService(service)
    }
}

fun Context.startServiceCompat(service: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(service)
    } else {
        startService(service)
    }
}