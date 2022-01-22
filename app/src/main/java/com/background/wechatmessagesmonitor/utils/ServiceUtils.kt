package com.background.wechatmessagesmonitor.utils

import android.app.ActivityManager
import android.app.Service
import android.content.Context

fun Context.isServiceWork(cls: Class<out Service>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = activityManager.getRunningServices(100)
    if (services.isNullOrEmpty()) {
        return false
    }
    for (service in services) {
        if (service.service.className == cls.name) {
            return true
        }
    }
    return false
}