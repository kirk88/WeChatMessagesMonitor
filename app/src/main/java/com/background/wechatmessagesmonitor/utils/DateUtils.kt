package com.background.wechatmessagesmonitor.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * com.tencent.mm  大万: 什么通知[害羞]  Bundle[{android.title=大万, android.reduced.images=true, android.subText=null, android.showChronometer=false, android.icon=2131233736, android.text=什么通知[害羞], android.progress=0, android.progressMax=0, android.appInfo=ApplicationInfo{4e15473 com.tencent.mm}, android.showWhen=true, android.largeIcon=android.graphics.Bitmap@f9e2f30, android.infoText=null, android.progressIndeterminate=false, android.remoteInputHistory=null}]
 * com.tencent.mm  大万: 这个可以吗  Bundle[{android.title=大万, android.reduced.images=true, android.subText=null, android.showChronometer=false, android.icon=2131233736, android.text=[2条]大万: 这个可以吗, android.progress=0, android.progressMax=0, android.appInfo=ApplicationInfo{80512e com.tencent.mm}, android.showWhen=true, android.largeIcon=android.graphics.Bitmap@6f2c4cf, android.infoText=null, android.progressIndeterminate=false, android.remoteInputHistory=null}]
 * com.tencent.mm  大万: [微信红包]恭喜发财，大吉大利  Bundle[{android.title=大万, android.reduced.images=true, android.subText=null, android.showChronometer=false, android.icon=2131233736, android.text=[微信红包]恭喜发财，大吉大利, android.progress=0, android.progressMax=0, android.appInfo=ApplicationInfo{406e2e1 com.tencent.mm}, android.showWhen=true, android.largeIcon=android.graphics.Bitmap@b155806, android.infoText=null, android.progressIndeterminate=false, android.remoteInputHistory=null}]
 *com.tencent.mm  大万: [转账] 请收款  Bundle[{android.title=大万, android.reduced.images=true, android.subText=null, android.showChronometer=false, android.icon=2131233736, android.text=[2条]大万: [转账] 请收款, android.progress=0, android.progressMax=0, android.appInfo=ApplicationInfo{65cccf4 com.tencent.mm}, android.showWhen=true, android.largeIcon=android.graphics.Bitmap@4adcc1d, android.infoText=null, android.progressIndeterminate=false, android.remoteInputHistory=null}]

 */


fun Long.toDate() = Date(this)

fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String{
    val formatter = SimpleDateFormat(pattern, Locale.CANADA)
    return formatter.format(this)
}