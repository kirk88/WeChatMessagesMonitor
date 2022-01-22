package com.background.wechatmessagesmonitor.data

import com.background.wechatmessagesmonitor.applicationContext
import com.background.wechatmessagesmonitor.constants.KEY_COMMISSIONER_ID
import com.background.wechatmessagesmonitor.utils.*

data class WechatMessage(
    val who: String?,
    val content: String,
    val time: String?,
    val deviceId: String?,
    val phone: String?,
    val commissionerId: String?,
    val type: Int
){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WechatMessage

        if (who != other.who) return false
        if (content != other.content) return false
        if (deviceId != other.deviceId) return false
        if (phone != other.phone) return false
        if (commissionerId != other.commissionerId) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = who?.hashCode() ?: 0
        result = 31 * result + content.hashCode()
        result = 31 * result + (deviceId?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (commissionerId?.hashCode() ?: 0)
        result = 31 * result + type
        return result
    }

}

fun createMessage(who: String?, content: String, time: String? = null, type: Int = 0): WechatMessage {
    return WechatMessage(
        who,
        content,
        time = time ?: System.currentTimeMillis().toDate().format(),
        deviceId = DeviceIdUtils.getDeviceIdCached(applicationContext),
        phone = PhoneUtils.getPhoneNumber(applicationContext),
        commissionerId = Prefs[KEY_COMMISSIONER_ID],
        type = type
    )
}
