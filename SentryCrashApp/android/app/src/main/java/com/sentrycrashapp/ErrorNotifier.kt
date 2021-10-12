package com.sentrycrashapp

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

interface IErrorNotifier {
    fun reportThrowable(error: Throwable?, additionalMessage: String? = null, logLevel: NotifierLogLevel = NotifierLogLevel.ERROR, tags: MutableMap<String, String> = mutableMapOf(), extra: MutableMap<String, String> = mutableMapOf())
    fun report(message: String, logLevel: NotifierLogLevel, tags: Map<String, String> = mapOf(), extra: Map<String, String> = mapOf())
    fun addBreadcrumb(message: String, logLevel: NotifierLogLevel, category: String, data: WritableMap = Arguments.createMap())
}

object ErrorNotifier : IErrorNotifier {
    const val EVENT_TYPE_KEY_REPORT = "ErrorNotifierRecordMessage"
    private const val TAGS_KEY = "tags"
    private const val EXTRA_KEY = "extra"

    private const val EVENT_TYPE_KEY_BREADCRUMB = "ErrorNotifierBreadcrumb"
    private const val DATA_KEY = "data"
    private const val CATEGORY_KEY = "category"

    private const val MESSAGE_KEY = "message"
    private const val LOG_LEVEL_KEY = "logLevel"

    override fun reportThrowable(error: Throwable?, additionalMessage: String?, logLevel: NotifierLogLevel, tags: MutableMap<String, String>, extra: MutableMap<String, String>) {
        if (error == null) {
            return
        }
        val message = error.toString()

        extra["additionalMessage"] = additionalMessage ?: "not provided"
        extra["exceptionClass"] = error.javaClass.toString()
        extra["rawMessage"] = error.message ?: "not provided"
        extra["trace"] = error.stackTrace.joinToString("\n")
        report(message, logLevel, tags, extra)
    }


    override fun report(message: String, logLevel: NotifierLogLevel, tags: Map<String, String>, extra: Map<String, String>) {
        val tagArgs = Arguments.createMap()
        val extraArgs = Arguments.createMap()
        tags.forEach {  tagArgs.putString(it.key, it.value) }
        extra.forEach {  extraArgs.putString(it.key, it.value) }
        val params = Arguments.createMap()
        params.putString(MESSAGE_KEY, message)
        params.putString(LOG_LEVEL_KEY, logLevel.logLevel)
        params.putMap(TAGS_KEY, tagArgs)
        params.putMap(EXTRA_KEY, extraArgs)

        EventEmitter.sendEvent(EVENT_TYPE_KEY_REPORT, params, true)
    }

    fun addBreadcrumb(message: String, logLevel: NotifierLogLevel, category: String, data: Map<String, String>) {
        val params = Arguments.createMap()
        data.forEach {  params.putString(it.key, it.value) }
        addBreadcrumb(message, logLevel, category, params)
    }

    override fun addBreadcrumb(message: String, logLevel: NotifierLogLevel, category: String, data: WritableMap) {
        val params = Arguments.createMap()
        params.putString(MESSAGE_KEY, message)
        params.putString(LOG_LEVEL_KEY, logLevel.logLevel)
        params.putString(CATEGORY_KEY, category)
        params.putMap(DATA_KEY, data)

        EventEmitter.sendEvent(EVENT_TYPE_KEY_BREADCRUMB, params, true)
    }
}