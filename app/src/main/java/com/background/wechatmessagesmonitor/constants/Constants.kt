package com.background.wechatmessagesmonitor.constants

const val NOTIFICATION_CHANNEL_ID = "wechatmessagesmonitor_service_notification"

const val NOTIFICATION_ID = 10000

const val KEY_NOTIFICATION_SERVICE_HAS_KILLED = "notification_service_has_killed"
const val KEY_ACCESSIBILITY_SERVICE_HAS_KILLED = "accessibility_service_has_killed"

const val KEY_COMMISSIONER_ID = "KEY_COMMISSIONER_ID"

const val WECHAT_PACKAGE_NAME = "com.tencent.mm"

val WECHAT_MESSAGE_CONTENT_EXTRA_REGEX = "\\[.*?\\].*?:".toRegex()
val WECHAT_MESSAGE_TIME_REGEX = ".*?[0-9]{1,2}:[0-9]{1,2}".toRegex()

const val WECHAT_MESSAGE_LIST_VIEW_ID = "com.tencent.mm:id/b79"
const val WECHAT_GROUND_VIEW_ID = "com.tencent.mm:id/b9_"

const val WECHAT_TIME_NODE_VIEW_ID = "com.tencent.mm:id/b8z"
const val WECHAT_AVATAR_NODE_VIEW_ID = "com.tencent.mm:id/b3s"
const val WECHAT_TEXT_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b4b"
const val WECHAT_IMAGE_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b4c"
const val WECHAT_AUDIO_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b9m"
const val WECHAT_VIDEO_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b77"
const val WECHAT_SHARE_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b2z"
const val WECHAT_FILE_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/b3l"
const val WECHAT_REDPACKET_NODE_VIEW_ID = "com.tencent.mm:id/y4"
const val WECHAT_REDPACKET_CONTENT_NODE_VIEW_ID = "com.tencent.mm:id/y0"
const val WECHAT_TRANSFER_NODE_VIEW_ID = "com.tencent.mm:id/yb"

const val BASE_URL = "https://t.zmn365.cn/api/weixin"