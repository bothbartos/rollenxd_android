package com.bartosboth.rollen_android.ui.components

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.model.user.UserDetail
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
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}


@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
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

@Composable
fun CoverImage(
    modifier: Modifier = Modifier,
    coverBase64: String,
    songId: String,
    size: Dp,
    shape: Shape = RoundedCornerShape(4.dp),
    shadowElevation: Dp = 0.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = shape,
        shadowElevation = shadowElevation
    ) {
        val isPreview = LocalInspectionMode.current

        if (isPreview) {
            // Use a simple Box with a color for preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(convertBase64ToByteArr(coverBase64))
                    .memoryCacheKey(songId)
                    .placeholderMemoryCacheKey(songId)
                    .build(),
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SongInfo(
    modifier: Modifier = Modifier,
    title: String?,
    artist: String?,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    artistStyle: TextStyle = MaterialTheme.typography.titleSmall
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
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
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
    var sliderPosition by remember { mutableFloatStateOf(progress / 100f) }
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
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(7.dp)
            .clickable { onClick() },
        colors = cardColors,

        ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            CoverImage(
                coverBase64 = song.coverBase64,
                songId = song.id.toString(),
                size = 160.dp,
                shadowElevation = 4.dp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Text(
                text = song.author,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SongListRowItem(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(7.dp)
            .clickable { onClick() },
        colors = cardColors,

        ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp)
        ) {
            CoverImage(
                coverBase64 = song.coverBase64,
                songId = song.id.toString(),
                size = 130.dp,
                shadowElevation = 4.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Text(
                    text = song.author,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PlaylistListItem(
    playlist: PlaylistData,
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
            .padding(7.dp)
            .clickable { onClick() },
        colors = cardColors
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp)
        ) {
            CoverImage(
                coverBase64 = playlist.coverBase64,
                songId = playlist.id.toString(),
                size = 160.dp,
                shadowElevation = 4.dp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Text(
                text = playlist.author,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
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

@Composable
fun MiniPlayerBar(
    progress: Float,
    audio: Song,
    isAudioPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onBarClick: () -> Unit,
    onLike: (Long) -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    userDetail: UserDetail,
    currentPlayingAudioId: Long = -1L,
    isLiked: Boolean
) {
    BottomAppBar(
        modifier = Modifier.height(if (currentPlayingAudioId == -1L) 91.dp else 150.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        content = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                if (currentPlayingAudioId != -1L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(3.dp)
                            .clickable { onBarClick() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,

                        ) {
                        CoverImage(
                            coverBase64 = audio.coverBase64,
                            songId = audio.id.toString(),
                            size = 53.dp
                        )

                        SongInfo(
                            title = audio.title,
                            artist = audio.author,
                            modifier = Modifier.weight(1f)
                        )

                        LikeButton(isLiked = isLiked, onClick = { onLike(audio.id) })

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onHomeClick) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home"
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularBase64ImageButton(
                            userDetail = userDetail,
                            onClick = onProfileClick,
                            contentDescription = "Profile",
                            size = 30.dp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun CircularBase64ImageButton(
    userDetail: UserDetail,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 56.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val imageKey = remember(userDetail.profileImageBase64) {
        "${userDetail.id}_${userDetail.profileImageBase64.hashCode()}"
    }
    Log.d("IMAGEKEY", "CircularBase64ImageButton: $imageKey")

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(convertBase64ToByteArr(userDetail.profileImageBase64))
                .memoryCacheKey(imageKey)
                .placeholderMemoryCacheKey(imageKey)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: List<AppBarAction> = emptyList()
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = if (onBack != null) 0.dp else 16.dp)
            ) {
                onBack?.let {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .clickable { it() }
                            .padding(end = 8.dp)
                    )
                }
                Text(title)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        actions = {
            if (actions.isNotEmpty()) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        actions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.title) },
                                onClick = {
                                    action.onClick()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun WelcomeLogo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(100.dp),
            painter = painterResource(R.drawable.rollenxdicon),
            contentDescription = "RollenXD Logo"
        )
        Text(
            text = "Welcome to RollenXD",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun UploadFab(
    onUploadSong: () -> Unit,
    onCreatePlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showMenu = !showMenu },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (showMenu) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (showMenu) "Close menu" else "Open menu",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 72.dp)
            ) {
                FabOption(
                    icon = painterResource(R.drawable.music_note),
                    label = "Upload Song",
                    onClick = {
                        onUploadSong()
                        showMenu = false
                    }
                )

                FabOption(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Create Playlist",
                    onClick = {
                        onCreatePlaylist()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp)
        )
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun FabOption(
    icon: Painter,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp)
        )
        Icon(
            painter = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UploadSongDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, audioFile: Uri?, coverImage: Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var audioError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            if (mimeType == "audio/mpeg") {
                audioUri = uri
                audioError = null
            } else {
                audioError = "Please select an MP3 file"
                Toast.makeText(context, "Please select an MP3 file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> coverUri = uri }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Upload Song",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { audioLauncher.launch("audio/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select Audio File")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (audioUri != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Audio selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { imageLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select Cover Image")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (coverUri != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Image selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onUpload(title, audioUri, coverUri)
                            onDismiss()
                        },
                        enabled = title.isNotBlank() && audioUri != null && coverUri != null
                    ) {
                        Text("Upload")
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, songId: List<Long>) -> Unit,
    songs: List<Song>
) {
    var title by remember { mutableStateOf("") }
    var songIds by remember { mutableStateOf(emptyList<Long>()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Playlist Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    itemsIndexed(songs) { index, song ->
                        SongListRowItem(
                            song = song,
                            isPlaying = songIds.contains(song.id),
                            onClick = {
                                songIds = if (songIds.contains(song.id)) {
                                    songIds.filter { it != song.id }
                                } else {
                                    songIds + song.id
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreate(title, songIds)
                            onDismiss()
                        },
                        enabled = title.isNotBlank() && songIds.isNotEmpty()
                    ) {
                        Text("Create Playlist")
                    }
                }

            }

        }
    }

}


@Preview
@Composable
fun BottomBarPreview() {
    MiniPlayerBar(
        progress = 1.0f,
        audio = Song(
            title = "Title",
            author = "Author",
            coverBase64 = "",
            length = 100.0,
            isLiked = false,
            reShares = 0,
            id = 1L
        ),
        isAudioPlaying = true,
        onPlayPauseClick = { },
        onBarClick = { },
        onLike = { },
        onHomeClick = {},
        onSearchClick = { },
        onProfileClick = {},
        userDetail = UserDetail(
            id = 1L,
            name = "username",
            bio = "bio",
            profileImageBase64 = "",
            email = "email",
            songs = emptyList()
        ),
        isLiked = false,
    )
}
