package com.background.wechatmessagesmonitor.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.background.wechatmessagesmonitor.constants.KEY_NOTIFICATION_SERVICE_HAS_KILLED
import com.background.wechatmessagesmonitor.constants.NOTIFICATION_ID
import com.background.wechatmessagesmonitor.constants.WECHAT_NOTIFICATION_MESSAGE_CONTENT_EXTRA_REGEX
import com.background.wechatmessagesmonitor.constants.WECHAT_PACKAGE_NAME
import com.background.wechatmessagesmonitor.data.MessagesUploadManager
import com.background.wechatmessagesmonitor.data.model.createMessage
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.Prefs
import com.background.wechatmessagesmonitor.utils.createNotification

class WechatNotificationService : NotificationListenerService() {

    private var messageIndex: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug { "WechatNotificationService onStartCommand" }
        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != WECHAT_PACKAGE_NAME) return
        val extras = sbn.notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val content = extras.getString(Notification.EXTRA_TEXT)?.replace(
            WECHAT_NOTIFICATION_MESSAGE_CONTENT_EXTRA_REGEX,
            ""
        )?.trim() ?: return

        Logger.debug { "onNotificationPosted  $extras" }

        val message = kotlin.runCatching {
            createMessage(
                title,
                content,
                from = "未知",
                type = 0,
                index = messageIndex
            )
        }.getOrNull()
        if (message != null) {
            MessagesUploadManager.addMessage(message)
        }

        messageIndex += 1
    }

    override fun onListenerConnected() {
        Logger.debug { "WechatNotificationService onListenerConnected" }
    }

    override fun onListenerDisconnected() {
        Logger.debug { "WechatNotificationService onListenerDisconnected" }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()

        Prefs[KEY_NOTIFICATION_SERVICE_HAS_KILLED] = false

        Logger.debug { "WechatNotificationService onCreate" }
    }

    override fun onDestroy() {
        stopForeground()
        super.onDestroy()

        Prefs[KEY_NOTIFICATION_SERVICE_HAS_KILLED] = true

        Logger.debug { "WechatNotificationService onDestroy" }
    }

    private fun startForeground() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForeground() {
        stopForeground(false)
    }

}