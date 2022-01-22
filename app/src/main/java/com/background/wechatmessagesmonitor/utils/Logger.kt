package com.background.wechatmessagesmonitor.utils

import com.background.wechatmessagesmonitor.BuildConfig
import java.util.logging.Level

object Logger {

    private val logger = java.util.logging.Logger.getLogger("WechatMonitor")

    fun debug(message: ()-> Any?) {
        if (BuildConfig.DEBUG) {
            logger.log(Level.INFO, message().toString())
        }
    }

    @JvmOverloads
    fun error(error: Throwable, message: () -> Any? = { error.message ?: "Error" }) {
        if (BuildConfig.DEBUG) {
            logger.log(Level.WARNING, message().toString(), error)
        }
    }

}