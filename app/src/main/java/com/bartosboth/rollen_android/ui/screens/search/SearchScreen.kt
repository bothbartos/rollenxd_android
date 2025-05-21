package com.bartosboth.rollen_android.ui.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.ui.components.MiniPlayerBar
import com.bartosboth.rollen_android.ui.components.SongListRowItem
import com.bartosboth.rollen_android.ui.navigation.MainScreen
import com.bartosboth.rollen_android.ui.navigation.PlayerScreen
import com.bartosboth.rollen_android.ui.navigation.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchState: SearchState,
    onTextChange: (String) -> Unit,
    searchResult: List<Song>,
    navController: NavController,
    onBackClick: () -> Unit,
    progress: Float,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Song,
    onCurrentSongLike: (Long) -> Unit,
    userDetail: UserDetail,
    onStart: () -> Unit,
    onSongClick: (Long) -> Unit,
    isCurrentSongLiked: Boolean,
    searchQuery: String
) {
    var searchQuery by remember { mutableStateOf(searchQuery) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onTextChange(it)
                        },
                        placeholder = { Text(text = "Search...")},
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            onTextChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
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
                onSearchClick = { },
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
            color = MaterialTheme.colorScheme.background
        ){
            when(searchState){
                is SearchState.Error -> {
                    Box(contentAlignment = Alignment.Center){
                        Text(text = "Error: ${searchState.message}")
                    }
                }
                SearchState.Idle -> {
                    Box(contentAlignment = Alignment.Center){
                        Text(text = "Search for songs by title or author name")
                    }
                }
                SearchState.Loading -> {
                    Box(contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                }
                SearchState.Success -> {
                    if(searchResult.isEmpty()){
                        Box(contentAlignment = Alignment.Center){
                            Text(
                                text = "No songs found",
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(16.dp),
                        ) {
                            itemsIndexed(searchResult) { index, song ->
                                SongListRowItem(
                                    song = song,
                                    isPlaying = song.id == currentPlayingAudio.id,
                                    onClick = { onSongClick(song.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
