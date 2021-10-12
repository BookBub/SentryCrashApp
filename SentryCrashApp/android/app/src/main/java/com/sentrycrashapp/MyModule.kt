package com.sentrycrashapp

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import kotlin.concurrent.thread

class MyModule internal constructor(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String {
        return "MyModule"
    }

    @ReactMethod
    fun throwException(msg: String) {
        thread() {
            val message = "Not an int"
            message.toInt()
        }
    }
}