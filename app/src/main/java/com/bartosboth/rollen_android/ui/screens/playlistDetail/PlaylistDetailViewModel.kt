package com.bartosboth.rollen_android.ui.screens.playlistDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.ui.navigation.PlaylistDetailScreen
import com.bartosboth.rollen_android.ui.screens.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

        private val playlistId: Long = savedStateHandle.get<Long>(PlaylistDetailScreen.playlistIdArg) ?: -1L

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

    val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Idle)
    val playlistState = _playlistState.asStateFlow()

    init{
        if(playlistId != -1L){
            getPlaylist(playlistId)
        }
    }


    fun getPlaylist(id: Long){
        viewModelScope.launch {
            try {
                _playlistState.value = PlaylistState.Loading

                val response = playlistRepository.getPlaylistById(id)
                response.let {
                    _playlist.value = it
                    _playlistState.value = PlaylistState.Success
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
