package com.background.wechatmessagesmonitor.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.background.wechatmessagesmonitor.constants.*
import com.background.wechatmessagesmonitor.data.MessagesUploadManager
import com.background.wechatmessagesmonitor.data.WechatMessage
import com.background.wechatmessagesmonitor.data.createMessage
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.Prefs
import com.background.wechatmessagesmonitor.utils.createNotification
import java.lang.Exception

class WechatAccessibilityService : AccessibilityService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug { "WechatAccessibilityService onStartCommand" }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow
        uploadMessages(rootNode)

//        logNode(rootNode)
    }

    private fun logNode(node: AccessibilityNodeInfo?, prefix: String = "") {
        node ?: return
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                Logger.debug { "$prefix${child.viewIdResourceName}  ${child.contentDescription}  ${child.text}" }
            } catch (ex: Exception) {
            }
            logNode(child, "    ")
        }
    }

    private fun uploadMessages(node: AccessibilityNodeInfo?) {
        node ?: return
        val contentNode =
            node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b79")
                ?.firstOrNull()
                ?: return

        val messages = mutableListOf<WechatMessage>()

        contentNode.children().forEach {
            var name: String? = null
            var nameIndex = 0
            var content: String? = null
            var contentIndex = 0
            var mediaType: String? = null

            for (child in it.children()) {
                Logger.debug { "${child.viewIdResourceName}  ${child.contentDescription}  ${child.text}" }

                if (child.viewIdResourceName == WECHAT_TIME_NODE_VIEW_ID) {
                    messages.clear()
                }

                if (child.viewIdResourceName == WECHAT_AVATAR_NODE_VIEW_ID) {
                    name = child.contentDescription?.replace("头像".toRegex(), "")
                }

                for ((i, c) in child.children().withIndex()) {
                    val id = c.viewIdResourceName

                    if (id == WECHAT_AVATAR_NODE_VIEW_ID && name == null) {
                        name = c.contentDescription?.replace("头像".toRegex(), "")
                        nameIndex = i
                    }

                    if (id == WECHAT_REDPACKET_NODE_VIEW_ID && content == null) {
                        content = c.text?.toString()
                        contentIndex = i
                    }

                    if (id == WECHAT_TRANSFER_NODE_VIEW_ID && content == null) {
                        content = c.text?.toString()
                        contentIndex = i
                    }

                    if (id == WECHAT_SHARE_CONTENT_NODE_VIEW_ID && content == null) {
                        content = "[分享]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id == WECHAT_FILE_CONTENT_NODE_VIEW_ID && content == null) {
                        content = "[文件]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id == WECHAT_VIDEO_CONTENT_NODE_VIEW_ID && content == null) {
                        content = "[视频]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id == WECHAT_IMAGE_CONTENT_NODE_VIEW_ID && content == null) {
                        content = "[图片]"
                        contentIndex = i
                    }

                    if (id == WECHAT_AUDIO_CONTENT_NODE_VIEW_ID) {
                        mediaType = "[语音]"
                    }

                    if (id == WECHAT_TEXT_CONTENT_NODE_VIEW_ID && content == null) {
                        content = "${mediaType.orEmpty()}${c.text?.toString()}"
                        contentIndex = i
                    }
                }
                if (nameIndex > contentIndex) {
                    Logger.debug { "自己发送的" }
                }
                if (name != null && content != null) {
                    Logger.debug { "onAccessibilityEvent: $name  $content" }
                    val message =
                        kotlin.runCatching { createMessage(name, content, type = 1) }.getOrNull()
                    if (message != null) {
                        messages.add(message)
                    }
                }
            }
        }

        MessagesUploadManager.addMessages(messages)
    }

    private fun AccessibilityNodeInfo.children(): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        for (index in 0 until childCount) {
            val child = getChild(index) ?: continue
            nodes.add(child)
        }
        return nodes
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