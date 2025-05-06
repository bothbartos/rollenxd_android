package com.bartosboth.rollen_android.data.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 2,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 2,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                    )
                    .setBackBuffer(150000, true)
                    .build()
            )
            .build()
            .also { player ->
                Log.d("DI", "ExoPlayer instance created: $player")
            }
    }

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, exoPlayer)
        .build()

    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer,
        @ApplicationContext context: Context
    ): SongServiceHandler = SongServiceHandler(exoPlayer, context)

}