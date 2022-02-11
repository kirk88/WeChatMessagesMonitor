package com.background.wechatmessagesmonitor.data

import android.util.LruCache
import android.view.accessibility.AccessibilityNodeInfo
import com.background.wechatmessagesmonitor.applicationContext
import com.background.wechatmessagesmonitor.constants.*
import com.background.wechatmessagesmonitor.data.model.WechatMessage
import com.background.wechatmessagesmonitor.data.model.createMessage
import com.background.wechatmessagesmonitor.service.WechatForegroundService
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.isServiceWork
import com.background.wechatmessagesmonitor.utils.startServiceCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

@OptIn(DelicateCoroutinesApi::class)
object MessagesUploadManager {

    private val cacheMessages = LruCache<Int, WechatMessage>(6000)

    private val channel = Channel<WechatMessage>()

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.error(throwable) { "Receive Message" }
    }

    fun addMessagesFromNode(nodeInfo: AccessibilityNodeInfo) =
        GlobalScope.launch(Dispatchers.Default + errorHandler) {
            val messages = getMessages(nodeInfo)

            addMessages(messages)
        }

    fun addMessage(message: WechatMessage) = addMessages(listOf(message))

    fun addMessages(messages: Collection<WechatMessage>) =
        GlobalScope.launch(Dispatchers.Default + errorHandler) {
            addMessagesInternal(messages)
        }

    fun collectMessages(block: suspend (WechatMessage) -> Unit): Job =
        GlobalScope.launch(Dispatchers.IO + errorHandler) {
            for (message in channel) {
                block(message)
            }
        }

    private suspend fun addMessagesInternal(messages: Collection<WechatMessage>) {
        if (!applicationContext.isServiceWork(WechatForegroundService::class.java)) {
            applicationContext.startServiceCompat(WechatForegroundService::class.java)
        }

        for (msg in messages) {
            val key = msg.hashCode()
            if (cacheMessages.get(key) == null) {
                Logger.debug { msg }
                cacheMessages.put(key, msg)
                channel.send(msg)
            }
        }
    }

    private fun getMessages(node: AccessibilityNodeInfo?): List<WechatMessage> {
        node ?: return emptyList()
        val contentNode = node.findContentNode() ?: return emptyList()
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

        return messages
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

}