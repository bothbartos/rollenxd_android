package com.bartosboth.rollen_android.ui.screens.playlistDetail

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.CoverImage

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    playlistState: PlaylistState,
    onBackClick: () -> Unit = {}){
    Scaffold(
        topBar = {
            AppTopBar(
                title = playlist.title,
                onBack = onBackClick,
            )
        }
    ){innerPadding ->
        if(playlistState is PlaylistState.Loading){
            CircularProgressIndicator()
        }else{
            Column(modifier = Modifier.padding(innerPadding)) {
                CoverImage(
                    coverBase64 = playlist.coverBase64,
                    songId = playlist.id,
                    size = 50.dp
                )
                Text(text = playlist.title)
                Text(text = playlist.author)

            }
        }

    }

}