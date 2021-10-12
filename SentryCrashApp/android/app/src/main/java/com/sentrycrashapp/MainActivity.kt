package com.sentrycrashapp

import android.os.Bundle
import android.os.StrictMode

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.ReactRootView
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView

class MainActivity : ReactActivity() {
    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    override fun getMainComponentName(): String {
        return "SentryCrashApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var skipSplashScreen = false
        intent.extras?.let {
            skipSplashScreen = it.getBoolean("SkipSplashScreen")
        }

        enableStrictMode()
    }

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        // we were already using this, kotlin compiler is just louder about it than java
        @Suppress("DEPRECATION")
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun createRootView(): ReactRootView {
                return RNGestureHandlerEnabledRootView(this@MainActivity)
            }
        }
    }

    private fun enableStrictMode() {
        println("enabling strict mode")
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())

    }
}