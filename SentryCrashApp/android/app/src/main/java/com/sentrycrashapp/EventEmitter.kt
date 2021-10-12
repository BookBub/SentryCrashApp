package com.sentrycrashapp

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class RNRetryEvent(val eventName: String, val params: WritableMap)

object EventEmitter {
    private const val FLUSH_QUEUE_MS = 1000L
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val queuedEvents = mutableListOf<RNRetryEvent>()

    private var reactContext: ReactContext? = null

    val instance: RCTDeviceEventEmitter?
        get() {
            reactContext?.let {
                if (it.hasActiveCatalystInstance()) {
                    return it.getJSModule(RCTDeviceEventEmitter::class.java)
                }
            }
            return null
        }

    fun attachReactContext(newReactContext: ReactContext) {
        reactContext = newReactContext

        scheduler.schedule({
            flushQueuedEvents()
        }, FLUSH_QUEUE_MS, TimeUnit.MILLISECONDS)
    }

    // We'd like to avoid using `WritableMap` when possible
    // This is an overloaded version of this function that can take a map of String to String
    // Only use this if you intend to read the values as strings in JS as this will fail at casting other values
    fun sendEvent(eventName: String, params: Map<String, String>, retryDelivery: Boolean = false) {
        this.sendEvent(eventName, Arguments.makeNativeMap(params), retryDelivery)
    }

    fun sendEvent(eventName: String, params: WritableMap, retryDelivery: Boolean = false) {
        val emitter = instance
        if (emitter != null) {
            emitter.emit(eventName, params)
        } else {
            if (retryDelivery) {
                val event = RNRetryEvent(eventName, params)
                queuedEvents.add(event)
            }
            Log.d(EventEmitter::class.java.simpleName, "Cannot emit an event before a react context is attached and set up")
        }
    }

    private fun flushQueuedEvents() {
        queuedEvents.forEach {
            it.params.putBoolean("delayed", true)
            sendEvent(it.eventName, it.params)
        }
        queuedEvents.clear()
    }
}