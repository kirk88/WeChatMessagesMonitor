package com.background.wechatmessagesmonitor.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.background.wechatmessagesmonitor.constants.NOTIFICATION_ID
import com.background.wechatmessagesmonitor.data.MessagesUploadManager
import com.background.wechatmessagesmonitor.data.WechatMessage
import com.background.wechatmessagesmonitor.data.createMessage
import com.background.wechatmessagesmonitor.utils.Logger
import com.background.wechatmessagesmonitor.utils.createNotification
import com.background.wechatmessagesmonitor.utils.toJson
import com.nice.kothttp.OkRequestMethod
import com.nice.kothttp.httpCallBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class WechatForegroundService : Service() {

    private var uploadJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug { "WechatForegroundService onStartCommand" }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()

        Logger.debug { "WechatForegroundService onCreate" }

        uploadJob = MessagesUploadManager.collectMessages {
            uploadMessage(it)
        }
    }

    override fun onDestroy() {
        stopForeground()
        super.onDestroy()

        Logger.debug { "WechatForegroundService onDestroy" }

        uploadJob?.cancel()
    }

    private fun startForeground() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForeground() {
        stopForeground(true)
    }

    private suspend fun uploadMessage(message: WechatMessage) {
        httpCallBuilder<String>(OkRequestMethod.Post)
            .requestBody {
                message.toJson().toRequestBody("application/json".toMediaType())
            }
            .make()
            .catch {
                Logger.error(it){ "Upload Message" }
            }
            .collect()
    }

}