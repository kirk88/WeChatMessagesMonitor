package com.background.wechatmessagesmonitor.constants

const val NOTIFICATION_CHANNEL_ID = "wechatmessagesmonitor_service_notification"

const val NOTIFICATION_ID = 10000

const val KEY_NOTIFICATION_SERVICE_HAS_KILLED = "notification_service_has_killed"
const val KEY_ACCESSIBILITY_SERVICE_HAS_KILLED = "accessibility_service_has_killed"

const val KEY_COMMISSIONER_ID = "KEY_COMMISSIONER_ID"

const val WECHAT_PACKAGE_NAME = "com.tencent.mm"

val WECHAT_NOTIFICATION_MESSAGE_CONTENT_EXTRA_REGEX = "\\[.*?\\].*?:".toRegex()

val WECHAT_MESSAGE_LIST_VIEW_IDS = arrayOf("com.tencent.mm:id/b79")
val WECHAT_GROUND_JUDGE_VIEW_IDS = arrayOf("com.tencent.mm:id/b9_")

val WECHAT_TIME_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b8z"
)
val WECHAT_AVATAR_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b3s"
)
val WECHAT_TEXT_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b4b"
)
val WECHAT_IMAGE_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b4c"
)
val WECHAT_AUDIO_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b9m"
)
val WECHAT_VIDEO_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b77"
)
val WECHAT_SHARE_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b2z"
)
val WECHAT_FILE_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/b3l"
)
val WECHAT_REDPACKET_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/y4"
)
val WECHAT_REDPACKET_CONTENT_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/y0"
)
val WECHAT_TRANSFER_NODE_VIEW_IDS = arrayOf(
    "com.tencent.mm:id/yb"
)

const val BASE_URL = "https://t.zmn365.cn/api/weixin"