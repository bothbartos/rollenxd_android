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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
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
    addComment: (Long, String) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Back button
            TopAppBar(
                title = { Text("") },
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
                            songId = songId.toString(),
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

                when (commentState) {

                    CommentState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.size(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    CommentState.Idle -> {
                        item {
                            Box(
                                modifier = Modifier.size(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    is CommentState.Error -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) {

                                    Text(text = commentState.message)
                                }
                            }
                        }
                    }

                    CommentState.Success -> {
                        item {
                            if(comments.isNotEmpty()){
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                ) {
                                    itemsIndexed(comments) { index, comment ->
                                        CommentRow(comment = comment)
                                    }
                                }
                            }else{
                                Box(modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "No comments yet")
                                }
                            }
                        }
                        item {
                            var text by remember {
                                mutableStateOf("")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    placeholder = { Text("Add comment") }
                                )
                                IconButton(
                                    onClick = { addComment(songId, text) },
                                    enabled = text.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        tint = if (text.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5f
                                        ),
                                        contentDescription = "Send comment"
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


@Preview
@Composable
fun CommentRow(
    comment: Comment = Comment(
        id = 1L,
        songId = 1L,
        userId = 1L,
        username = "test",
        profilePicture = "",
        text = "new comment"
    )
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .width(500.dp)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(80.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CoverImage(
                    coverBase64 = comment.profilePicture,
                    songId = comment.username + comment.id,
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
            )
        }
    }
}
