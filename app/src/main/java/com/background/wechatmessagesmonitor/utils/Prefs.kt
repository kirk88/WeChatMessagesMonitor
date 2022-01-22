package com.background.wechatmessagesmonitor.utils

import android.content.Context
import com.background.wechatmessagesmonitor.applicationContext

object Prefs {

    private val preferences by lazy {
        val name = applicationContext.packageName + "_preferences"
        applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    operator fun set(key: String, value: Any?) {
        val editor = preferences.edit()
        if (value == null) {
            editor.remove(key).apply()
            return
        }
        when (value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Set<*> -> editor.putStringSet(key, value.map { it.toString() }.toSet())
            else -> error("Unsupported value type: ${value.javaClass.name}")
        }
        editor.apply()
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String): T? {
        val value = preferences.all[key] ?: return null
        return value as? T
    }

    operator fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

}


fun <T : Any> Prefs.getOrDefault(key: String, defValue: T): T {
    return get(key) ?: defValue
}

fun <T : Any> Prefs.getOrElse(key: String, defValue: () -> T): T {
    return get(key) ?: defValue()
}