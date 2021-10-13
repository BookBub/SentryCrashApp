package com.sentrycrashapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaDescription
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media.utils.MediaConstants
import java.net.URI

class BackgroundAudioService : MediaBrowserServiceCompat() {

    private lateinit var mediaCallback: MediaSessionCallback
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var notificationManager: MediaNotificationManager
    private val binder = this.LocalBinder()
    private lateinit var preferences: Preferences

    @RequiresApi(Build.VERSION_CODES.M)
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
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP)
                .build())

        mediaSession.isActive = true
        val builder = MediaDescription.Builder()
        builder.setMediaId(defaultMediaItem.mediaId)
        builder.setTitle(defaultMediaItem.title)
        builder.setSubtitle(defaultMediaItem.subtitle)
        builder.setMediaUri(defaultMediaItem.mediaUri)
        mediaSession.setQueue(listOf(MediaSessionCompat.QueueItem(MediaDescriptionCompat.fromMediaDescription(builder.build()), defaultMediaItem.mediaId.toLong())))
        notificationManager = MediaNotificationManager(this)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        val extras = Bundle()
        extras.putBoolean("android.media.browse.CONTENT_STYLE_SUPPORTED", true)
        // Set default content style to grid for playable items (audiobooks)
        extras.putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM)
        return BrowserRoot("/", extras)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // Should crash
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

    override fun onDestroy() {
        mediaSession.release()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationManager.getNotification(mediaSession)
        startForeground(MediaNotificationManager.NOTIFICATION_ID, notification)
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        if (SERVICE_INTERFACE == intent.action) {
            return super.onBind(intent);
        }

        return binder
    }
}

class MediaNotificationManager(private val service: BackgroundAudioService) {

    private val playAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_play,
            "play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this.service,
                    PlaybackStateCompat.ACTION_PLAY))

    private val pauseAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_pause,
            "pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this.service,
                    PlaybackStateCompat.ACTION_PAUSE))

    private val jumpFwdAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_fwd_15,
            "next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this.service,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT))

    private val jumpBackAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_back_15,
            "previous",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this.service,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))

    private val notificationManager: NotificationManager = this.service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        cancelNotifications()
    }

    fun cancelNotifications() {
        notificationManager.cancelAll()
    }

    fun getNotification(mediaSession: MediaSessionCompat): Notification {
        val state = mediaSession.controller.playbackState

        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val builder = buildNotification(isPlaying)
        return builder.build()
    }

    private fun buildNotification(isPlaying: Boolean): NotificationCompat.Builder {

        // Create the (mandatory) notification channel when running on Android Oreo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val builder = NotificationCompat.Builder(service, CHANNEL_ID)
        builder.apply {
            setSmallIcon(R.drawable.ic_headphones)
            priority = NotificationCompat.PRIORITY_LOW
            // Pending intent that is fired when user clicks on notification.
            setContentIntent(createContentIntent())
            setContentTitle(defaultMediaItem.title)
            setContentText(defaultMediaItem.subtitle)
            setOngoing(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    // For backwards compatibility with Android L and earlier.
                    .setShowCancelButton(true)
            )

            if (defaultMediaItem.mediaId != DEFAULT_MEDIA_ID) {
                setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        // indices of actions added below
                        .setShowActionsInCompactView(0, 1, 2)
                )
                // ACTIONS ARE ADDED IN THE ORDER THEY WILL APPEAR
                addAction(jumpBackAction)
                addAction(if (isPlaying) pauseAction else playAction)
                addAction(jumpFwdAction)
            }
        }

        return builder
    }

    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            val name = "Chirp Books"
            // The user-visible description of the channel.
            val description = "Audiobook Player"
            // No sound or visual interruption
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.apply {
                this.description = description
                enableLights(true)
                lightColor = Color.BLUE
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "createChannel: New channel created")
        } else {
            Log.d(TAG, "createChannel: Existing channel reused")
        }
    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(service, MainActivity::class.java)
        openUI.putExtra("SkipSplashScreen", true)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
                service, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    companion object {
        private val TAG = MediaNotificationManager::class.java.simpleName
        private const val CHANNEL_ID = "chirp_playback_channel"
        private const val REQUEST_CODE = 501
        const val NOTIFICATION_ID = 412
        const val DEFAULT_MEDIA_ID = "default_media_id"
    }
}