package com.background.wechatmessagesmonitor.utils

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.background.wechatmessagesmonitor.applicationContext

private val ToastHandler by lazy {
    Handler(Looper.getMainLooper())
}

fun showToast(msg: CharSequence) {
    val toast = Runnable {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    if (Looper.getMainLooper() == Looper.myLooper()) {
        toast.run()
    } else {
        ToastHandler.post(toast)
    }
}

fun showLongToast(msg: CharSequence) {
    val toast = Runnable {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
    }
    if (Looper.getMainLooper() == Looper.myLooper()) {
        toast.run()
    } else {
        ToastHandler.post(toast)
    }
}