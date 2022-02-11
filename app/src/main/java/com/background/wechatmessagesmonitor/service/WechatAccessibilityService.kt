package com.background.wechatmessagesmonitor.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.background.wechatmessagesmonitor.constants.KEY_ACCESSIBILITY_SERVICE_HAS_KILLED
import com.background.wechatmessagesmonitor.constants.NOTIFICATION_ID
import com.background.wechatmessagesmonitor.data.MessagesUploadManager
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.Prefs
import com.background.wechatmessagesmonitor.utils.createNotification

class WechatAccessibilityService : AccessibilityService() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug { "WechatAccessibilityService onStartCommand" }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        rootInActiveWindow?.let {
            MessagesUploadManager.addMessagesFromNode(it)

            //        logNode(it)
        }
    }

    private fun logNode(node: AccessibilityNodeInfo?, prefix: String = "") {
        node ?: return
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            Logger.debug { "${node.viewIdResourceName} ${node.contentDescription}  ${node.text}" }
            try {
                Logger.debug { "$prefix${child.viewIdResourceName}  ${child.contentDescription}  ${child.text}" }
            } catch (ex: Exception) {
            }
            logNode(child, "    ")
        }
    }

    override fun onInterrupt() {
        Logger.debug { "WechatAccessibilityService onInterrupt" }
    }

    override fun onServiceConnected() {
        Logger.debug { "WechatAccessibilityService onServiceConnected" }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()

        Prefs[KEY_ACCESSIBILITY_SERVICE_HAS_KILLED] = false

        Logger.debug { "WechatAccessibilityService onCreate" }
    }

    override fun onDestroy() {
        stopForeground()
        super.onDestroy()

        Prefs[KEY_ACCESSIBILITY_SERVICE_HAS_KILLED] = true

        Logger.debug { "WechatAccessibilityService onDestroy" }
    }

    private fun startForeground() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForeground() {
        stopForeground(false)
    }

}