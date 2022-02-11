package com.background.wechatmessagesmonitor.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.background.wechatmessagesmonitor.constants.*
import com.background.wechatmessagesmonitor.data.MessagesUploadManager
import com.background.wechatmessagesmonitor.data.model.WechatMessage
import com.background.wechatmessagesmonitor.data.model.createMessage
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.Prefs
import com.background.wechatmessagesmonitor.utils.createNotification

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
            Logger.debug { "${node.viewIdResourceName} ${node.contentDescription}  ${node.text}" }
            try {
                Logger.debug { "$prefix${child.viewIdResourceName}  ${child.contentDescription}  ${child.text}" }
            } catch (ex: Exception) {
            }
            logNode(child, "    ")
        }
    }

    private fun uploadMessages(node: AccessibilityNodeInfo?) {
        node ?: return
        val contentNode = node.findContentNode() ?: return
        val isFromGroup = contentNode.isFromGroup()

        val messages = mutableListOf<WechatMessage>()

        var messageIndex = 0
        var time: String? = null

        contentNode.children().forEach {
            var name: String? = null
            var nameIndex = 0
            var content: String? = null
            var contentIndex = 0

            var redPacketContent: String? = null
            var mediaType: String? = null

            it.findTimeNode()?.text?.toString()?.let { text ->
                time = text
            }

            for (child in it.children()) {
                if (child.viewIdResourceName in WECHAT_AVATAR_NODE_VIEW_IDS) {
                    name = child.contentDescription?.replace("头像".toRegex(), "")
                }

                for ((i, c) in child.children().withIndex()) {
                    val id = c.viewIdResourceName

                    if (id in WECHAT_AVATAR_NODE_VIEW_IDS && name == null) {
                        name = c.contentDescription?.replace("头像".toRegex(), "")
                        nameIndex = i
                    }

                    if (id in WECHAT_REDPACKET_CONTENT_NODE_VIEW_IDS) {
                        redPacketContent = c.text?.toString()
                    }

                    if (id in WECHAT_REDPACKET_NODE_VIEW_IDS && content == null) {
                        content = "[${c.text?.toString()}]${redPacketContent.orEmpty()}"
                        contentIndex = i
                    }

                    if (id in WECHAT_TRANSFER_NODE_VIEW_IDS && content == null) {
                        content = "[${c.text?.toString()}]"
                        contentIndex = i
                    }

                    if (id in WECHAT_SHARE_CONTENT_NODE_VIEW_IDS && content == null) {
                        content = "[分享]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id in WECHAT_FILE_CONTENT_NODE_VIEW_IDS && content == null) {
                        content = "[文件]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id in WECHAT_VIDEO_CONTENT_NODE_VIEW_IDS && content == null) {
                        content = "[视频]${c.text?.toString()}"
                        contentIndex = i
                    }

                    if (id in WECHAT_IMAGE_CONTENT_NODE_VIEW_IDS && content == null) {
                        content = "[图片]"
                        contentIndex = i
                    }

                    if (id in WECHAT_AUDIO_CONTENT_NODE_VIEW_IDS) {
                        mediaType = "[语音]"
                    }

                    if (id in WECHAT_TEXT_CONTENT_NODE_VIEW_IDS && content == null) {
                        content = "${mediaType.orEmpty()}${c.text?.toString()}"
                        contentIndex = i
                    }
                }
                if (nameIndex > contentIndex) {
                    Logger.debug { "自己发送的" }
                }
                if (name != null && content != null) {
                    Logger.debug { "onAccessibilityEvent: $time  $name  $content" }
                    val message = kotlin.runCatching {
                        createMessage(
                            name,
                            content,
                            time = time,
                            from = if (isFromGroup) "群聊" else "个人",
                            type = 1,
                            index = messageIndex
                        )
                    }.getOrNull()

                    if (message != null) {
                        messages.add(message)
                    }

                    messageIndex += 1
                }
            }
        }

        MessagesUploadManager.addMessages(messages)
    }

    private fun AccessibilityNodeInfo.findContentNode(): AccessibilityNodeInfo? {
        for (viewId in WECHAT_MESSAGE_LIST_VIEW_IDS) {
            val node = findAccessibilityNodeInfosByViewId(viewId)
                ?.firstOrNull()
            if (node != null) {
                return node
            }
        }
        return null
    }

    private fun AccessibilityNodeInfo.findTimeNode(): AccessibilityNodeInfo? {
        for (viewId in WECHAT_TIME_NODE_VIEW_IDS) {
            val node = findAccessibilityNodeInfosByViewId(viewId)
                ?.firstOrNull()
            if (node != null) {
                return node
            }
        }
        return null
    }

    private fun AccessibilityNodeInfo.isFromGroup(): Boolean {
        for (viewId in WECHAT_GROUND_JUDGE_VIEW_IDS) {
            val nodes = findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }
        return false
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