package com.sentrycrashapp

import android.os.Binder
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

class BackgroundAudioService : MediaBrowserServiceCompat() {

    private lateinit var mediaCallback: MediaSessionCallback
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val binder = this.LocalBinder()
    private lateinit var preferences: Preferences

    override fun onCreate() {
        super.onCreate()
        preferences = Preferences(applicationContext)
        preferences.setSharedPreference()

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "tag").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
//            setCallback(MySessionCallback())
//
            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        mediaCallback = MediaSessionCallback()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        preferences.getSharedPreference()
        return null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        preferences.getSharedPreference()
    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            mediaSession.isActive = true
            preferences.getSharedPreference()
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: BackgroundAudioService
            // Return this instance of BackgroundAudioService so clients can call public methods
            get() = this@BackgroundAudioService
    }
}