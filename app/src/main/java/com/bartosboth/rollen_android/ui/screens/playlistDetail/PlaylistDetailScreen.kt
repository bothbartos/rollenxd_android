package com.bartosboth.rollen_android.ui.screens.playlistDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.CoverImage
import com.bartosboth.rollen_android.ui.components.LargePlayPauseButton
import com.bartosboth.rollen_android.ui.components.LikeButton
import com.bartosboth.rollen_android.ui.components.MiniPlayerBar
import com.bartosboth.rollen_android.ui.navigation.MainScreen
import com.bartosboth.rollen_android.ui.navigation.PlayerScreen
import com.bartosboth.rollen_android.ui.navigation.ProfileScreen
import com.bartosboth.rollen_android.ui.navigation.SearchScreen

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    playlistState: PlaylistState,
    onBackClick: () -> Unit = {},
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Song,
    onCurrentSongLike: (Long) -> Unit,
    onSongLike: (Long) -> Unit,
    playPlaylist: (Long) -> Unit,
    userDetail: UserDetail,
    onStart: () -> Unit,
    onPlaylistSongPlay: (Long, Long) -> Unit,
    isCurrentSongLiked: Boolean,
    likedSongIds: Set<Long>,
    navController: NavController
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "",
                onBack = onBackClick,
            )
        },
        bottomBar = {
            MiniPlayerBar(
                progress = progress,
                audio = currentPlayingAudio,
                isAudioPlaying = isAudioPlaying,
                onPlayPauseClick = onStart,
                onLike = onCurrentSongLike,
                onHomeClick = { navController.navigate(MainScreen) },
                onSearchClick = { navController.navigate(SearchScreen) },
                onProfileClick = { navController.navigate(ProfileScreen) },
                onBarClick = { navController.navigate(PlayerScreen) },
                userDetail = userDetail,
                currentPlayingAudioId = currentPlayingAudio.id,
                isLiked = isCurrentSongLiked
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface
        ) {
            when (playlistState) {
                PlaylistState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                PlaylistState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PlaylistState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = playlistState.message)
                    }
                }

                PlaylistState.Success -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CoverImage(
                                    coverBase64 = playlist.coverBase64,
                                    songId = playlist.id.toString(),
                                    size = 250.dp
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(
                                            start = 30.dp,
                                            end = 16.dp,
                                            bottom = 16.dp
                                        )
                                ) {
                                    Text(
                                        text = playlist.title,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = playlist.author,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                LargePlayPauseButton(
                                    modifier = Modifier.padding(end = 16.dp),
                                    isPlaying = isAudioPlaying
                                ) { playPlaylist(playlist.id) }
                            }
                        }

                        if (playlist.songs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No songs",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(playlist.songs) { index, song ->
                                val surfaceColour =
                                    if (currentPlayingAudio.id == song.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                PlaylistSongRow(
                                    surfaceColour = surfaceColour,
                                    song = song,
                                    playlistId = playlist.id,
                                    onSongLike = onSongLike,
                                    onPlaylistSongPlay = onPlaylistSongPlay,
                                    isLiked = likedSongIds.contains(song.id)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistSongRow(
    surfaceColour: Color,
    song: Song,
    playlistId: Long,
    onSongLike: (Long) -> Unit,
    onPlaylistSongPlay: (Long, Long) -> Unit,
    isLiked: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = surfaceColour,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onPlaylistSongPlay(song.id, playlistId) }
        ) {
            CoverImage(
                coverBase64 = song.coverBase64,
                songId = song.id.toString(),
                size = 50.dp,
                modifier = Modifier.padding(start = 8.dp)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = song.author,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            LikeButton(
                isLiked = isLiked,
                onClick = { onSongLike(song.id) }
            )
        }
    }
}

@Preview
@Composable
fun PlaylistDetailScreenPreview(
    playlist: Playlist = Playlist(
        id = 1L,
        title = "Playlist",
        author = "author",
        coverBase64 = "",
        songs = listOf(
            Song(
                title = "Song 1",
                author = "author",
                coverBase64 = "",
                length = 10.0,
                isLiked = false,
                reShares = 1,
                id = 1L
            ),
            Song(
                title = "Song 2",
                author = "author",
                coverBase64 = "",
                length = 10.0,
                isLiked = false,
                reShares = 1,
                id = 2L
            ),
            Song(
                title = "Song 3",
                author = "author",
                coverBase64 = "",
                length = 10.0,
                isLiked = false,
                reShares = 1,
                id = 3L
            )
        )
    )
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,

        ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoverImage(
                        coverBase64 = playlist.coverBase64,
                        songId = playlist.id.toString(),
                        size = 250.dp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 30.dp,
                            end = 30.dp,
                            bottom = 16.dp
                        )
                    ) {
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = playlist.author,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    LargePlayPauseButton(
                        modifier = Modifier.padding(end = 16.dp),
                        isPlaying = false
                    ) { }
                }
            }

            if (playlist.songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            } else {
                itemsIndexed(playlist.songs) { index, song ->
                    val surfaceColour = MaterialTheme.colorScheme.surfaceVariant
                    PlaylistSongRow(
                        surfaceColour = surfaceColour,
                        song = song,
                        playlistId = playlist.id,
                        onSongLike = {},
                        onPlaylistSongPlay = { _, _ -> },
                        isLiked = false
                    )
                }
            }
        }
    }
}