package com.bartosboth.rollen_android.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.ui.components.CoverImage
import com.bartosboth.rollen_android.ui.components.LargePlayPauseButton
import com.bartosboth.rollen_android.ui.components.LikeButton
import com.bartosboth.rollen_android.ui.components.MediaControlButton
import com.bartosboth.rollen_android.ui.components.ProgressSlider
import com.bartosboth.rollen_android.ui.components.SongInfo
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents
import com.bartosboth.rollen_android.utils.convertBase64ToByteArr
import com.bartosboth.rollen_android.utils.timeStampToDuration

@Composable
fun PlayerScreen(
    viewModel: AudioViewModel,
    navController: NavController
) {
    val isPlaying = viewModel.isPlaying

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .weight(0.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Back Arrow"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // Middle section with album cover
            Box(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CoverImage(
                    coverBase64 = viewModel.currentSelectedAudio.coverBase64,
                    songId = viewModel.currentSelectedAudio.id,
                    size = 300.dp,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 8.dp
                )
            }

            // Bottom section with controls
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Song info and like button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongInfo(
                        title = viewModel.currentSelectedAudio.title,
                        artist = viewModel.currentSelectedAudio.author,
                        titleStyle = MaterialTheme.typography.headlineSmall,
                        artistStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    LikeButton(
                        isLiked = viewModel.currentSelectedAudio.isLiked,
                        onClick = {
                            if (viewModel.currentSelectedAudio.isLiked) {
                                viewModel.unlikeSong(viewModel.currentSelectedAudio.id)
                            } else {
                                viewModel.likeSong(viewModel.currentSelectedAudio.id)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                ProgressSlider(
                    progress = viewModel.progress,
                    duration = viewModel.duration,
                    currentTime = viewModel.progressString,
                    totalDuration = viewModel.currentSelectedAudio.length,
                    onSeek = { viewModel.onUiEvent(UiEvents.SeekTo(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    MediaControlButton(
                        icon = R.drawable.skip_prev,
                        contentDescription = "Previous",
                        onClick = { viewModel.onUiEvent(UiEvents.Backward) }
                    )

                    // Play/Pause button
                    LargePlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = { viewModel.onUiEvent(UiEvents.PlayPause) }
                    )

                    // Next button
                    MediaControlButton(
                        icon = R.drawable.skip_next,
                        contentDescription = "Next",
                        onClick = { viewModel.onUiEvent(UiEvents.SeekToNext) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
