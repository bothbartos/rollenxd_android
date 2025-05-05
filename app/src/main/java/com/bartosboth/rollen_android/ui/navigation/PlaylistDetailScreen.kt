package com.bartosboth.rollen_android.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDetailScreen(val playlistId: Long) {
    companion object {
        const val route = "playlist_detail"
        const val playlistIdArg = "playlistId"
    }
}
