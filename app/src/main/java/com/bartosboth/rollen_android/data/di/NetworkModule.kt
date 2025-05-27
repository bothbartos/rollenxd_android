package com.bartosboth.rollen_android.data.di

import android.content.Context
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.network.AuthAPI
import com.bartosboth.rollen_android.data.network.AuthInterceptor
import com.bartosboth.rollen_android.data.network.CommentAPI
import com.bartosboth.rollen_android.data.network.PlaylistAPI
import com.bartosboth.rollen_android.data.network.SearchAPI
import com.bartosboth.rollen_android.data.network.SongAPI
import com.bartosboth.rollen_android.data.network.UserDetailAPI
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
object NetworkModule {

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
            .client(okHttpClient)
            .build()
            .create(SongAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthAPI(okHttpClient: OkHttpClient): AuthAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(AuthAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideUserDetailAPI(okHttpClient: OkHttpClient): UserDetailAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(UserDetailAPI::class.java)
    }

    @Provides
    @Singleton
    fun providePlaylistAPI(okHttpClient: OkHttpClient): PlaylistAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(PlaylistAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchAPI(okHttpClient: OkHttpClient): SearchAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SearchAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentAPI(okHttpClient: OkHttpClient): CommentAPI {
        return Retrofit.Builder()
            .baseUrl("http://${Constants.BASE_URL}/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CommentAPI::class.java)
    }
}