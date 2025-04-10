package com.bartosboth.rollen_android.ui.screens.main

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.data.model.song.Song
import kotlin.math.floor
import kotlin.math.roundToInt


@Composable
fun BottomBar(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    audio: Song,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar(
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioPlayer(
                        isAudioPlaying = isAudioPlaying,
                        onStart = onStart,
                        onNext = onNext
                    )
                    ArtistInfo(
                        audio = audio,
                        modifier = Modifier.weight(1f),
                    )

                }
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progress / 100 })
            }
        }
    )
}

@Composable
fun PlayerIcon(
    modifier: Modifier = Modifier,
    icon: Int,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null
            )

        }
    }

}

@Composable
fun ArtistInfo(modifier: Modifier = Modifier, audio: Song) {
    Log.d("ARTISTINFO", "ArtistInfo: ${audio.author} - ${audio.title}")
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = audio.title ?: "Unknown title",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Clip,
                modifier = modifier.weight(1f),
                maxLines = 1
            )
            //Spacer(modifier = modifier.size(1.dp))
            Text(
                text = audio.author ?: "Unknown artist",
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AudioPlayer(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        PlayerIcon(
            icon = if (isAudioPlaying) R.drawable.pause
            else R.drawable.play_arrow,
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            painter = painterResource(R.drawable.skip_next),
            modifier = Modifier.clickable {
                onNext()
            },
            contentDescription = "next"
        )
    }

}

@Composable
fun MainScreen(
    navController: NavController,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Song,
    audioList: List<Song>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            BottomBar(
                progress = progress,
                onProgressChange = onProgressChange,
                isAudioPlaying = isAudioPlaying,
                audio = currentPlayingAudio,
                onNext = onNext,
                onStart = onStart
            )
        }
    ) {innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            itemsIndexed(audioList) { index, song ->
                AudioItem(audio = song, 
                    isPlaying = song.id == currentPlayingAudio.id) {
                    Log.d("LZYCLM_ID", "MainScreen: song clicked ${song.id}")
                    onItemClick(index)
                }
            }
        }
    }
}

@Composable
fun AudioItem(
    audio: Song,
    isPlaying: Boolean,
    onItemClick: () -> Unit
) {
    val cardColor = if (isPlaying) CardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.onSurface
    ) else CardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.onSurface,
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onItemClick()
            },
        colors = cardColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.title ?: "Unknown title",
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.author ?: "Unknown artist",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

            }
            Log.d("TAG", "AudioItem: ${audio.length}")
            Text(
                text = timeStampToDuration(audio.length)
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

    }
}

private fun timeStampToDuration(position: Double): String {
    if (position < 0) return "--:--"
    val totalSeconds = position.roundToInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}



@Preview()
@Composable
fun HomeScreenPrev() {
    BottomBar(
        progress = 0.5f,
        onProgressChange = {},
        audio = Song(
            uri = "".toUri(),
            author = "Dr. Assman",
            length = 10000.0,
            title = "Song Two",
            numberOfLikes = 1,
            reShares = 1,
            id = 1
        ),
        isAudioPlaying = true,
        onStart = {  },
    ) { }
}
