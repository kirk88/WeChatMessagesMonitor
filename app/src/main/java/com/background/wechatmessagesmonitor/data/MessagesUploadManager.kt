package com.background.wechatmessagesmonitor.data

import android.util.LruCache
import com.background.wechatmessagesmonitor.applicationContext
import com.background.wechatmessagesmonitor.service.WechatForegroundService
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.isServiceWork
import com.background.wechatmessagesmonitor.utils.startServiceCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

object MessagesUploadManager {

    private val cacheMessages = LruCache<Int, WechatMessage>(6000)

    private val channel = Channel<WechatMessage>()

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.error(throwable) { "Receive Message" }
    }

    fun addMessage(message: WechatMessage) = addMessages(listOf(message))

    fun addMessages(messages: Collection<WechatMessage>) =
        GlobalScope.launch(Dispatchers.IO + errorHandler) {
            delay(1000)

            if (!applicationContext.isServiceWork(WechatForegroundService::class.java)) {
                applicationContext.startServiceCompat(WechatForegroundService::class.java)
            }

            for (msg in messages) {
                delay(200)

                val key = msg.hashCode()
                if (cacheMessages.get(key) == null) {
                    Logger.debug { msg }
                    cacheMessages.put(key, msg)
                    channel.send(msg)
                }
            }
        }


    fun collectMessages(block: suspend (WechatMessage) -> Unit): Job =
        GlobalScope.launch(Dispatchers.IO + errorHandler) {
            for (message in channel) {
                block(message)
            }
        }


}