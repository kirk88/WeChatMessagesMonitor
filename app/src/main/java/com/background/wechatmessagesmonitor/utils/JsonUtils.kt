package com.background.wechatmessagesmonitor.utils

import com.google.gson.GsonBuilder

private val GSON = GsonBuilder()
    .setLenient()
    .serializeNulls()
    .create()

fun Any.toJson(): String = GSON.toJson(this)