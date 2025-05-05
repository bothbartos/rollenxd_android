package com.bartosboth.rollen_android.ui.screens.playlistDetail

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {


    private val _playlist = MutableStateFlow<Playlist>(
        value = Playlist(
            id = -1L,
            title = "",
            author = "",
            coverBase64 = "",
            songs = emptyList()
        )
    )
    val playlist: StateFlow<Playlist> = _playlist

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Idle)
    val playlistState = _playlistState.asStateFlow()

    fun getPlaylist(id: Long){
        viewModelScope.launch {
            try {
                _playlistState.value = PlaylistState.Loading
                if(id == 0L){
                    val likedSongs = playlistRepository.getLikedSongs()
                    likedSongs.forEach { it.isLiked = true }
                    likedSongs.let{
                        _playlist.value = Playlist(
                            id = 0L,
                            title = "Liked Songs",
                            author = "You",
                            coverBase64 = Constants.LIKED_SONG_BASE64,
                            songs = likedSongs
                        )
                        _playlistState.value = PlaylistState.Success
                    }

                }else{
                    val response = playlistRepository.getPlaylistById(id)
                    response.let {
                        _playlist.value = it
                        _playlistState.value = PlaylistState.Success
                    }
                }
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error("Loading error: ${e.message}")
            }
        }
    }

}

sealed class PlaylistState {
    data object Idle : PlaylistState()
    data object Loading : PlaylistState()
    data object Success : PlaylistState()
    data class Error(val message: String) : PlaylistState()
}
