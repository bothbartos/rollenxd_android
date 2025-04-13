package com.bartosboth.rollen_android.ui.screens.player

import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
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
            verticalArrangement = Arrangement.SpaceBetween // This will space children evenly
        ) {
            // Top section with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .weight(0.5f), // Small weight for the top section
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

            // Middle section with album cover (give it more weight)
            Box(
                modifier = Modifier
                    .weight(4f) // Larger weight for the album cover
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(300.dp),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(convertBase64ToByteArr(viewModel.currentSelectedAudio.coverBase64))
                            .memoryCacheKey(viewModel.currentSelectedAudio.id.toString())
                            .placeholderMemoryCacheKey(viewModel.currentSelectedAudio.id.toString())
                            .build(),
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom section with controls
            Column(
                modifier = Modifier
                    .weight(3f) // Good weight for controls section
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Song info and like button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Song and artist info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.currentSelectedAudio.title ?: "Unknown title",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = viewModel.currentSelectedAudio.author ?: "Unknown author",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Like/unlike button
                    IconButton(onClick = {
                        if(viewModel.currentSelectedAudio.isLiked)
                            viewModel.unlikeSong(viewModel.currentSelectedAudio.id)
                        else viewModel.likeSong(viewModel.currentSelectedAudio.id)
                    }) {
                        Icon(
                            imageVector = if(viewModel.currentSelectedAudio.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                PlayerProgressBar(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat button
                    IconButton(onClick = { /* Handle repeat */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Repeat",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    // Previous button
                    IconButton(
                        onClick = { viewModel.onUiEvent(UiEvents.Backward) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_prev),
                            contentDescription = "Previous",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play/Pause button
                    IconButton(
                        onClick = { viewModel.onUiEvent(UiEvents.PlayPause) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            painter = if (isPlaying) painterResource(R.drawable.pause) else painterResource(R.drawable.play_arrow),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next button
                    IconButton(
                        onClick = { viewModel.onUiEvent(UiEvents.SeekToNext) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = "Next",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat button
                    IconButton(onClick = { /* Handle repeat */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Repeat",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Add some bottom padding to prevent controls from being too close to the edge
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PlayerProgressBar(viewModel: AudioViewModel) {
    var sliderPosition by remember { mutableFloatStateOf(viewModel.progress / 100f) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.progress) {
        if (!isSeeking) {
            sliderPosition = viewModel.progress / 100f
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = { newPosition ->
                isSeeking = true
                sliderPosition = newPosition
            },
            onValueChangeFinished = {
                viewModel.onUiEvent(UiEvents.SeekTo(sliderPosition))
                isSeeking = false
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isSeeking) {
                    // Fixed calculation for predicted time
                    timeStampToDuration((sliderPosition * viewModel.duration).toDouble())
                } else {
                    viewModel.progressString
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = timeStampToDuration(viewModel.currentSelectedAudio.length),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
