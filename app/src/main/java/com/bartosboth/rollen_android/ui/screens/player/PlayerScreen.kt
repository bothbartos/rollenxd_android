package com.bartosboth.rollen_android.ui.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.ui.components.CoverImage
import com.bartosboth.rollen_android.ui.components.LargePlayPauseButton
import com.bartosboth.rollen_android.ui.components.LikeButton
import com.bartosboth.rollen_android.ui.components.MediaControlButton
import com.bartosboth.rollen_android.ui.components.ProgressSlider
import com.bartosboth.rollen_android.ui.components.SongInfo
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents

@Composable
fun PlayerScreen(
    navController: NavController,
    coverBase64: String,
    songId: Long,
    title: String,
    author: String,
    isLiked: Boolean,
    isPlaying: Boolean,
    progress: Float,
    progressString: String,
    duration: Long,
    totalDuration: Double,
    onLike: (Long) -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
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
                    coverBase64 = coverBase64,
                    songId = songId,
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
                        title = title,
                        artist = author,
                        titleStyle = MaterialTheme.typography.headlineSmall,
                        artistStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    LikeButton(
                        isLiked = isLiked,
                        onClick = { onLike(songId) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                ProgressSlider(
                    progress = progress,
                    duration = duration,
                    currentTime = progressString,
                    totalDuration = totalDuration,
                    onSeek = { onSeek(it) }
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
                        onClick = { onPrevious }
                    )

                    // Play/Pause button
                    LargePlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = { onPlayPause }
                    )

                    // Next button
                    MediaControlButton(
                        icon = R.drawable.skip_next,
                        contentDescription = "Next",
                        onClick = { onNext }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
