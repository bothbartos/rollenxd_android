package com.bartosboth.rollen_android.data.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.network.AuthInterceptor
import com.bartosboth.rollen_android.data.network.AuthService
import com.bartosboth.rollen_android.data.network.SongAPI
import com.bartosboth.rollen_android.data.player.notification.NotificationManager
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
import com.bartosboth.rollen_android.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, exoPlayer)
        .build()

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): NotificationManager = NotificationManager(context, exoPlayer)

    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer,
        @ApplicationContext context: Context
    ): SongServiceHandler = SongServiceHandler(exoPlayer, context)

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSongAPi(okHttpClient: OkHttpClient): SongAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)  // Add the client with interceptor
            .build()
            .create(SongAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthService(okHttpClient: OkHttpClient): AuthService {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(AuthService::class.java)
    }
}