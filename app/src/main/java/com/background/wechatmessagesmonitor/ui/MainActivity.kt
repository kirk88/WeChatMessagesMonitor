package com.background.wechatmessagesmonitor.ui

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.background.wechatmessagesmonitor.R
import com.background.wechatmessagesmonitor.constants.KEY_ACCESSIBILITY_SERVICE_HAS_KILLED
import com.background.wechatmessagesmonitor.constants.KEY_COMMISSIONER_ID
import com.background.wechatmessagesmonitor.constants.KEY_NOTIFICATION_SERVICE_HAS_KILLED
import com.background.wechatmessagesmonitor.data.createMessage
import com.background.wechatmessagesmonitor.service.WechatAccessibilityService
import com.background.wechatmessagesmonitor.service.WechatForegroundService
import com.background.wechatmessagesmonitor.service.WechatNotificationService
import com.background.wechatmessagesmonitor.utils.*
import com.nice.kothttp.OkRequestMethod
import com.nice.kothttp.httpCallBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val permissionsLauncher =
        PocketActivityResultLauncher(ActivityResultContracts.RequestMultiplePermissions())
    private val activityLauncher =
        PocketActivityResultLauncher(ActivityResultContracts.StartActivityForResult())

    private var button1: Button? = null
    private var button2: Button? = null
    private var button3: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsLauncher.register(this)
        activityLauncher.register(this)

        checkRequirePermissions()

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)

        button1?.setOnClickListener {
            showCommissionerIdInputDialog { updateButton1(true) }
        }

        button2?.setOnClickListener {
            checkAccessibilityServiceEnabled { on ->
                updateButton2(on)
            }
        }

        button3?.setOnClickListener {
            checkNotificationListenerServiceEnabled { on ->
                updateButton3(on)
            }
        }

        startServiceCompat(Intent(this, WechatForegroundService::class.java))
    }

    override fun onResume() {
        super.onResume()
        updateViewByServiceState()
    }

    private fun checkRequirePermissions() {
        permissionsLauncher.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_PHONE_NUMBERS
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_SMS
                )
            }
        ) { result ->
            if (result.any { !it.value }) {
                showToast("权限获取失败")
                finish()
            }
        }
    }

    private fun updateViewByServiceState() {
        checkCommissionerIdExists {
            updateButton1(it)
        }

        checkAccessibilityServiceEnabled(false) {
            updateButton2(it)
        }

        checkNotificationListenerServiceEnabled(false) {
            updateButton3(it)
        }
    }

    private fun updateButton1(flag: Boolean) {
        button1?.text = if (flag) "已设置" else "设置"
    }

    private fun updateButton2(flag: Boolean) {
        button2?.apply {
            text = if (flag) "已开启" else "开启"
            isEnabled = !flag
        }
        if (flag) {
            startAccessibilityService()
        }
    }

    private fun updateButton3(flag: Boolean) {
        button3?.apply {
            text = if (flag) "已开启" else "开启"
            isEnabled = !flag
        }
        if (flag) {
            startNotificationService()
        }
    }

    private fun checkAccessibilityServiceEnabled(
        toSetting: Boolean = true,
        done: (Boolean) -> Unit
    ) {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val services =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val serviceEnabled = services.any { it.id.contains(packageName) }
        Logger.debug { "AccessibilityServiceEnabled: $serviceEnabled" }
        if (serviceEnabled) {
            done.invoke(true)
        } else if (toSetting) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activityLauncher.launch(intent) {
                    checkAccessibilityServiceEnabled(false, done)
                }
            } catch (ignored: Exception) {
                showToast("请手动到设置界面开启无障碍服务授权")
                done.invoke(false)
            }
        } else {
            done.invoke(false)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun checkNotificationListenerServiceEnabled(
        toSetting: Boolean = true,
        done: (Boolean) -> Unit
    ) {
        val serviceEnabled =
            NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        Logger.debug { "NotificationListenerServiceEnabled: $serviceEnabled" }
        if (serviceEnabled) {
            done.invoke(true)
        } else if (toSetting) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activityLauncher.launch(intent) {
                    checkNotificationListenerServiceEnabled(false, done)
                }
            } catch (ignored: Exception) {
                showToast("请手动到设置界面开启通知使用权限")
                done.invoke(false)
            }
        } else {
            done.invoke(false)
        }
    }

    private fun checkCommissionerIdExists(done: (Boolean) -> Unit) {
        val id: String? = Prefs[KEY_COMMISSIONER_ID]
        if (id.isNullOrBlank()) {
            done.invoke(false)
        } else {
            done.invoke(true)
        }
    }

    private fun showCommissionerIdInputDialog(done: () -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_input, null)
        view.layoutParams = ViewGroup.LayoutParams(-1, -1)
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(view)
            .create()

        val edit = view.findViewById<EditText>(R.id.edit)
        val confirm = view.findViewById<Button>(R.id.confirm)

        edit.text = Prefs[KEY_COMMISSIONER_ID]

        confirm.setOnClickListener {
            val text = edit.text?.toString()
            if (text.isNullOrBlank()) {
                showToast("请输入专员ID")
            } else {
                Prefs[KEY_COMMISSIONER_ID] = text
                dialog.dismiss()
                done.invoke()
            }
        }

        dialog.show()
    }

    private fun startAccessibilityService() {
        Logger.debug { "startAccessibilityService..." }

        if (isServiceWork(WechatAccessibilityService::class.java)) {
            Logger.debug { "WechatAccessibilityService already worked" }
            return
        }

        if (Prefs.getOrDefault(KEY_ACCESSIBILITY_SERVICE_HAS_KILLED, false)) {
            setServiceEnabled(WechatAccessibilityService::class.java, false)
            setServiceEnabled(WechatAccessibilityService::class.java, true)
        } else {
            val service = Intent(this, WechatAccessibilityService::class.java)
            startServiceCompat(service)
        }
    }

    private fun startNotificationService() {
        Logger.debug { "startNotificationService..." }

        if (isServiceWork(WechatNotificationService::class.java)) {
            Logger.debug { "WechatNotificationService already worked" }
            return
        }

        if (Prefs.getOrDefault(KEY_NOTIFICATION_SERVICE_HAS_KILLED, false)) {
            setServiceEnabled(WechatNotificationService::class.java, false)
            setServiceEnabled(WechatNotificationService::class.java, true)
        } else {
            val service = Intent(this, WechatNotificationService::class.java)
            startServiceCompat(service)
        }
    }

    private fun setServiceEnabled(cls: Class<out Service>, enabled: Boolean) {
        val pm = packageManager
        if (enabled) {
            pm.setComponentEnabledSetting(
                ComponentName(this, cls),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            pm.setComponentEnabledSetting(
                ComponentName(this, cls),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    override fun onBackPressed() {
        if (KEY_COMMISSIONER_ID !in Prefs) {
            showToast("请设置专员ID")
            return
        }
        super.onBackPressed()
    }

}