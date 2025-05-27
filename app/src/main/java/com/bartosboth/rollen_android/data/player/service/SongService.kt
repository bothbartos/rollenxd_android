package com.bartosboth.rollen_android.data.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SongService : MediaSessionService() {
    @Inject lateinit var mediaSession: MediaSession
    @Inject lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        Log.d("SongService", "onCreate called")
        super.onCreate()
        createNotificationChannel()
        addSession(mediaSession)

        Log.d("SongService", "Service created and session added")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SongService", "onStartCommand called")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("CNC", "Creating notification channel")
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Music playback controls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("CNC", "Notification channel created")
        }
    }

    override fun onDestroy() {
        Log.d("SongService", "onDestroy called")
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "music_channel"
    }
}
