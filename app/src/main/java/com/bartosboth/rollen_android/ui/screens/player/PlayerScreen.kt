package com.bartosboth.rollen_android.ui.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.R
import com.bartosboth.rollen_android.data.model.comment.Comment
import com.bartosboth.rollen_android.ui.components.CoverImage
import com.bartosboth.rollen_android.ui.components.LargePlayPauseButton
import com.bartosboth.rollen_android.ui.components.LikeButton
import com.bartosboth.rollen_android.ui.components.MediaControlButton
import com.bartosboth.rollen_android.ui.components.ProgressSlider
import com.bartosboth.rollen_android.ui.components.SongInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    coverBase64: String,
    songId: Long,
    title: String,
    author: String,
    comments: List<Comment>,
    commentState: CommentState,
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
    when (commentState) {
        CommentState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        CommentState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is CommentState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = commentState.message)
            }
        }

        CommentState.Success -> {

            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            val listState = rememberLazyListState()

            Scaffold(
                modifier = Modifier.fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    // Back button
                    TopAppBar(
                        title = {Text("")},
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Back Arrow"
                                )
                            }
                        },
                        colors = TopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val middleSectionHeight = this.maxHeight * 0.5f
                    val bottomSectionHeight = this.maxHeight * 0.5f
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Middle section with album cover
                        item {
                            Box(
                                modifier = Modifier
                                    .height(middleSectionHeight)
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
                        }

                        // Bottom section with controls
                        item {
                            Column(
                                modifier = Modifier
                                    .height(bottomSectionHeight)
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
                                        onClick = onPrevious
                                    )

                                    // Play/Pause button
                                    LargePlayPauseButton(
                                        isPlaying = isPlaying,
                                        onClick = onPlayPause
                                    )

                                    // Next button
                                    MediaControlButton(
                                        icon = R.drawable.skip_next,
                                        contentDescription = "Next",
                                        onClick = onNext
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Comments
                        item {
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.displaySmall,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        itemsIndexed(comments) { index, comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ){
                                Column {
                                    CoverImage(
                                        coverBase64 = comment.profilePicture,
                                        songId = comment.id,
                                        size = 30.dp,
                                        shape = CircleShape,
                                        shadowElevation = 2.dp
                                    )
                                    Text(
                                        text = comment.username,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(top = 4.dp),
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(start = 16.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

            }
        }
    }


}
