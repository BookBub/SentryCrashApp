package com.sentrycrashapp

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class MyModule internal constructor(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String {
        return "MyModule"
    }

    @ReactMethod
    fun throwException(msg: String) {
        println("** throwing an exception **")
        throw IllegalAccessException(msg)
        println("** somehow handled the exception **")
    }
}