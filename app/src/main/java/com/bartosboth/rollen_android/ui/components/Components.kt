package com.bartosboth.rollen_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.utils.convertBase64ToByteArr
import com.bartosboth.rollen_android.utils.timeStampToDuration

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        singleLine = true,  // This prevents multi-line input
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ScreenContainer(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun MiniPlayerBar(
    progress: Float,
    audio: Song,
    isAudioPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onBarClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.clickable { onBarClick() },
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoverImage(
                        coverBase64 = audio.coverBase64,
                        songId = audio.id,
                        size = 53.dp
                    )

                    SongInfo(
                        title = audio.title,
                        artist = audio.author,
                        modifier = Modifier.weight(1f)
                    )

                    PlayPauseButton(
                        isPlaying = isAudioPlaying,
                        onClick = onPlayPauseClick
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progress / 100 }
                )
            }
        }
    )
}

@Composable
fun CoverImage(
    coverBase64: String,
    songId: Long,
    size: Dp,
    shape: Shape = RoundedCornerShape(4.dp),
    shadowElevation: Dp = 0.dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = shape,
        shadowElevation = shadowElevation
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(convertBase64ToByteArr(coverBase64))
                .memoryCacheKey(songId.toString())
                .placeholderMemoryCacheKey(songId.toString())
                .build(),
            contentDescription = "Cover",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SongInfo(
    title: String?,
    artist: String?,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    artistStyle: TextStyle = MaterialTheme.typography.titleSmall,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        Text(
            text = title ?: "Unknown title",
            fontWeight = FontWeight.Bold,
            style = titleStyle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Text(
            text = artist ?: "Unknown artist",
            style = artistStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size)
    ) {
        Icon(
            painter = if (isPlaying) painterResource(R.drawable.pause) else painterResource(R.drawable.play_arrow),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun LargePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
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
}

@Composable
fun MediaControlButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    iconSize: Dp = 36.dp
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun ProgressSlider(
    progress: Float,
    duration: Long,
    currentTime: String,
    totalDuration: Double,
    onSeek: (Float) -> Unit
) {
    var sliderPosition by remember  { mutableFloatStateOf(progress / 100f) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (!isSeeking) {
            sliderPosition = progress / 100f
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
                onSeek(sliderPosition)
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
                    timeStampToDuration((sliderPosition * duration / 1000).toDouble())
                } else {
                    currentTime
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = timeStampToDuration(totalDuration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val cardColors = if (isPlaying) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() },
        colors = cardColors
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
                Text(
                    text = song.title ?: "Unknown title",
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = song.author ?: "Unknown artist",
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            Text(text = timeStampToDuration(song.length))
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Like",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
