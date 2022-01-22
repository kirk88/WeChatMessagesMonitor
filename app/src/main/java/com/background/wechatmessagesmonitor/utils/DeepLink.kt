package com.background.wechatmessagesmonitor.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.background.wechatmessagesmonitor.R
import com.background.wechatmessagesmonitor.applicationContext

object DeepLink {

    private val scheme = applicationContext.getString(R.string.deeplink_scheme)
    private val host = applicationContext.getString(R.string.deeplink_host)

    @JvmStatic
    fun Intent.isFromDeepLink(): Boolean {
        return data?.isDeepLink() ?: false
    }

    @JvmStatic
    fun Uri.isDeepLink(): Boolean {
        return scheme == DeepLink.scheme && host == DeepLink.host
    }

    @JvmStatic
    fun Context.open(deeplink: String) {
        try {
            val uri = Uri.parse(deeplink)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(packageName)
            startActivity(intent)
        } catch (_: Throwable) {
        }
    }

    @JvmStatic
    fun Context.open(deeplink: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, deeplink)
            intent.setPackage(packageName)
            startActivity(intent)
        } catch (_: Throwable) {
        }
    }

}