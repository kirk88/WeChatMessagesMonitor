package com.background.wechatmessagesmonitor.utils

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager

object PhoneUtils {

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getPhoneNumber(context: Context): String {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val number = tm.line1Number
            if (!number.isNullOrBlank() && number.startsWith("+86")) {
                return number.replace("+86", "")
            }
            return number.orEmpty()
        } catch (ex: Exception) {
            Logger.error(ex){ "Get Phone Number" }
        }
        return ""
    }

}