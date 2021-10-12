package com.sentrycrashapp

import android.content.Context
import android.content.SharedPreferences
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

class MyModule internal constructor(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FOO", Context.MODE_PRIVATE)

    override fun getName(): String {
        return "MyModule"
    }

    @ReactMethod
    fun throwException(msg: String) {
        thread {
            val message = "Not an int"
            message.toInt()
        }
    }

    @ReactMethod
    fun setSharedPreference() {
        thread {
            sharedPreferences
                    .edit()
                    .putString("aaa", Json.encodeToString(Data.serializer(), Data(42, "str")))
                    .apply()
        }
    }

    @ReactMethod
    fun getSharedPreference() {
        thread {
            Json.decodeFromString(AudiobookMetadata.serializer(), sharedPreferences.getString("aaa", null)!!)
        }
    }
}