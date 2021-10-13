package com.sentrycrashapp

import android.content.*
import android.net.Uri
import android.os.IBinder
import androidx.core.content.ContextCompat.startActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

data class MediaItemData(
        val mediaId: String,
        val title: String,
        val subtitle: String,
        val browsable: Boolean,
        var playbackRes: Int,
        var mediaUri: Uri
)

val defaultMediaItem = MediaItemData("1", "Media Item", "default", true, 1, Uri.parse("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_700KB.mp3"))

class MyModule internal constructor(val context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private var audioService: BackgroundAudioService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to BackgroundAudioService, cast the IBinder and get BackgroundAudioService instance
            if (service is BackgroundAudioService.LocalBinder) {
                audioService = service.service
                Preferences(context).setSharedPreference()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // only called when the service crashes
            ErrorNotifier.report("background audio service unexpectedly disconnected", NotifierLogLevel.WARNING)
        }
    }

    init {
        EventEmitter.attachReactContext(context)

        try {
            val intent = Intent(reactApplicationContext, BackgroundAudioService::class.java)
            reactApplicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            ErrorNotifier.report("bind background audio service failed", NotifierLogLevel.WARNING)

        }
    }

    private fun launchStartedService() {
        try {
            val intent = Intent(reactApplicationContext, BackgroundAudioService::class.java)
            reactApplicationContext.startService(intent)
        } catch (e: Exception) {
            ErrorNotifier.report("start background audio service failed", NotifierLogLevel.WARNING)
        }
    }

    override fun getName(): String {
        return "MyModule"
    }

    @ReactMethod
    fun throwException(msg: String) {
        val message = "Not an int"
        message.toInt()
    }

    @ReactMethod
    fun getSharedPreference() {
        Preferences(this.context).getSharedPreference()
    }

    @ReactMethod
    fun play() {
        launchStartedService()
        val intent = Intent(context, MediaPlayerActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context, intent, null)
    }
}