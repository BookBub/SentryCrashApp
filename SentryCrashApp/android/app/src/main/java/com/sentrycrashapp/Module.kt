package com.sentrycrashapp

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class Module internal constructor(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String {
        return "Module"
    }

    @ReactMethod
    fun throwException(msg: String) {
        throw IllegalAccessException(msg)
    }
}