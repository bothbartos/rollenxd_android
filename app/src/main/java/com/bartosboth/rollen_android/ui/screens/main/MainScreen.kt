package com.bartosboth.rollen_android.ui.screens.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.ErrorMessage
import com.bartosboth.rollen_android.ui.components.MiniPlayerBar
import com.bartosboth.rollen_android.ui.components.PlaylistListItem
import com.bartosboth.rollen_android.ui.components.SongListItem
import com.bartosboth.rollen_android.ui.components.UploadFab
import com.bartosboth.rollen_android.ui.navigation.MainScreen
import com.bartosboth.rollen_android.ui.navigation.PlayerScreen
import com.bartosboth.rollen_android.ui.navigation.ProfileScreen
import com.bartosboth.rollen_android.ui.navigation.SearchScreen
import com.bartosboth.rollen_android.ui.screens.audio.UiState


@Composable
fun MainScreen(
    userDetail: UserDetail,
    navController: NavController,
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Song,
    currentPlayingPlaylist: Playlist,
    audioList: List<Song>,
    playlists: List<PlaylistData>,
    onStart: () -> Unit,
    onSongClick: (Int) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onLike: (Long) -> Unit,
    uiState: UiState,
    isLiked: Boolean
) {

    Scaffold(
        topBar = {
            AppTopBar(
                title = "RollenXd",
            )
        },
        bottomBar = {
            MiniPlayerBar(
                progress = progress,
                audio = currentPlayingAudio,
                isAudioPlaying = isAudioPlaying,
                onPlayPauseClick = onStart,
                onLike = onLike,
                onHomeClick = { navController.navigate(MainScreen) },
                onSearchClick = { navController.navigate(SearchScreen) },
                onProfileClick = { navController.navigate(ProfileScreen) },
                onBarClick = { navController.navigate(PlayerScreen) },
                userDetail = userDetail,
                currentPlayingAudioId = currentPlayingAudio.id,
                isLiked = isLiked
            )
        },
        floatingActionButton = {
            UploadFab(
                onUploadSong = {  },
                onCreatePlaylist = { /* Handle later */ }
            )

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                UiState.Initial -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading songs...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ErrorMessage(message = uiState.message)
                    }
                }

                UiState.Ready -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Songs:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 7.dp, top = 4.dp)
                        )
                        LazyRow {
                            itemsIndexed(audioList) { index, song ->
                                SongListItem(
                                    song = song,
                                    isPlaying = song.id == currentPlayingAudio.id,
                                    onClick = { onSongClick(index) }
                                )
                            }
                        }
                        Text(
                            text = "Playlists:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 7.dp, top = 4.dp)
                        )
                        LazyRow {
                            itemsIndexed(playlists) { index, playlist ->
                                Log.d(
                                    "PLAYLISTMAINSCREEN",
                                    "MainScreen: Playlist id: ${playlist.id} currentplaylist id ${currentPlayingPlaylist.id}"
                                )
                                if (playlist.author == userDetail.name) {
                                    PlaylistListItem(
                                        playlist = playlist.copy(author = "You"),
                                        isPlaying = playlist.id == currentPlayingPlaylist.id,
                                        onClick = { onPlaylistClick(playlist.id) },
                                    )
                                } else {
                                    PlaylistListItem(
                                        playlist = playlist,
                                        isPlaying = playlist.id == currentPlayingPlaylist.id,
                                        onClick = { onPlaylistClick(playlist.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
