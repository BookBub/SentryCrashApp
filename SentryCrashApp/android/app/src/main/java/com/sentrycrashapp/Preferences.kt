package com.sentrycrashapp

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json

class Preferences(val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FOO", Context.MODE_PRIVATE)

    fun setSharedPreference() {
        sharedPreferences
                .edit()
                .putString("aaa", Json.encodeToString(Data.serializer(), Data(42, "str")))
                .apply()
    }

    fun getSharedPreference() {
        Json.decodeFromString(AudiobookMetadata.serializer(), sharedPreferences.getString("aaa", null)!!)
    }
}