package com.background.wechatmessagesmonitor

import android.app.Application
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.background.wechatmessagesmonitor.constants.BASE_URL
import com.background.wechatmessagesmonitor.constants.NOTIFICATION_CHANNEL_ID
import com.background.wechatmessagesmonitor.utils.DeviceIdUtils
import com.background.wechatmessagesmonitor.utils.Logger
import com.nice.kothttp.OkHttpConfiguration
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextHolder.init(this)

        initNotificationChannel()

        OkHttpConfiguration.Setter()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder().addInterceptor(
                    HttpLoggingInterceptor().setLevel(
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                    )
                ).build()
            )
            .apply()

        Logger.debug { "设备id: ${DeviceIdUtils.getDeviceId(applicationContext)}" }
    }

    private fun initNotificationChannel() {
        val managerCompat = NotificationManagerCompat.from(this)

        if (managerCompat.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID) != null) {
            return
        }

        val channelCompat = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MAX
        ).setName("啄木鸟助手服务通知")
            .setDescription("重要通知，必须开启")
            .build()

        managerCompat.createNotificationChannel(channelCompat)
    }

}

private object ApplicationContextHolder {

    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

}

val applicationContext: Context
    get() = ApplicationContextHolder.applicationContext