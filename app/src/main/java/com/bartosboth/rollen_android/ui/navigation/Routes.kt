package com.bartosboth.rollen_android.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object RegisterScreen

@Serializable
object LoginScreen

@Serializable
object MainFlow

@Serializable
object MainScreen

@Serializable
object PlayerScreen

@Serializable
object ProfileScreen

@Serializable
data class PlaylistDetailScreen(val playlistId: Long) {
    companion object {
        const val PLAYLIST_ID_ARG = "playlistId"
    }
}

@Serializable
data class SongDetailScreen(val songId: Long) {
    companion object {
        const val SONG_ID_ARG = "songId"
    }
}

@Serializable
object SearchScreen