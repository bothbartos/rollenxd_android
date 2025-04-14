package com.bartosboth.rollen_android.ui.screens.main

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.MiniPlayerBar
import com.bartosboth.rollen_android.ui.components.SongListItem
import com.bartosboth.rollen_android.ui.navigation.MainScreen
import com.bartosboth.rollen_android.ui.navigation.PlayerScreen
import com.bartosboth.rollen_android.ui.screens.audio.UiState


@Composable
fun MainScreen(
    logoutViewModel: LogoutViewModel,
    userDetailViewModel: UserDetailViewModel,
    navController: NavController,
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Song,
    audioList: List<Song>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onLike: (Long) -> Unit,
    uiState: UiState
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    logoutViewModel.logout()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "RollenXd",
                onLogoutClick = { showLogoutDialog = true },
            )
        },
        bottomBar = {
            userDetailViewModel.userDetails.collectAsState().value?.let {
                MiniPlayerBar(
                    progress = progress,
                    audio = currentPlayingAudio,
                    isAudioPlaying = isAudioPlaying,
                    onPlayPauseClick = onStart,
                    onLike = onLike,
                    onHomeClick = { navController.navigate(MainScreen) },
                    onSearchClick = { },
                    onProfileClick = { },
                    onBarClick = { navController.navigate(PlayerScreen) },
                    userDetail = it
                )
            }
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
                    // Show loading indicator
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

                UiState.Ready -> {
                    // Show content when ready
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
                                    onClick = { onItemClick(index) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
