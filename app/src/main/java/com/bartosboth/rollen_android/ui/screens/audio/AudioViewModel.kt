@file:OptIn(SavedStateHandleSaveableApi::class)

package com.bartosboth.rollen_android.ui.screens.audio

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.AudioRepository
import com.bartosboth.rollen_android.data.player.service.AudioState
import com.bartosboth.rollen_android.data.player.service.PlayerEvent
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val audioDummy = Song(
    title = "No song selected",
    author = "Unknown",
    coverBase64 = "",
    length = 0.0,
    isLiked = false,
    reShares = 0,
    id = -1L
)

private val playlistDummy = Playlist(
    id = -1L,
    title = "No playlist selected",
    author = "Unknown",
    coverBase64 = "",
    songs = emptyList(),
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val audioRepo: AudioRepository,
    private val playlistRepo: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var duration by savedStateHandle.saveable { mutableLongStateOf(1L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }

    private val _currentSelectedAudio = mutableStateOf(audioDummy)
    val currentSelectedAudio: Song get() = _currentSelectedAudio.value

    private val _audioList = mutableStateOf<List<Song>>(emptyList())
    val audioList: List<Song> get() = _audioList.value

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _playlists = mutableStateOf<List<PlaylistData>>(emptyList())
    val playlists: List<PlaylistData> get() = _playlists.value

    private val _selectedPlaylist = mutableStateOf<Playlist>(playlistDummy)
    val selectedPlaylist: Playlist get() = _selectedPlaylist.value

    private val _likedSongs = mutableStateOf<List<Song>>(emptyList())
    val likedSongs: List<Song> get() = _likedSongs.value

    init {
        loadAudioData()
        observePlayerEvents()
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            try {
                val songs = audioRepo.getAudioData()
                val playlist = playlistRepo.getPlaylists()
                val likedSongs = audioRepo.getLikedSongs()
                val likedSongPlaylist = PlaylistData(
                    id = 0L,
                    title = "Liked Songs",
                    author = "You",
                    coverBase64 = "",
                )
                _audioList.value = songs
                _likedSongs.value = likedSongs
                _playlists.value = listOf(likedSongPlaylist) + playlist
                _uiState.value = UiState.Ready
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playPlaylist(id: Long) {
        Log.d("PLAYLIST_ID", "playPlaylist: $id")
        viewModelScope.launch {
            try {
                var playlist =
                    when (id) {
                        0L -> {
                            Playlist(
                                id = 0L,
                                title = "Liked Songs",
                                author = "You",
                                coverBase64 = Constants.LIKED_SONG_BASE64,
                                songs = likedSongs
                            )
                        }

                        else -> {
                            playlistRepo.getPlaylistById(id)

                        }
                    }
                _selectedPlaylist.value = playlist
                songServiceHandler.setMediaItemList(emptyList())
                val mediaItem = playlist.songs.map { createMediaItem(it) }

                songServiceHandler.setMediaItemList(mediaItems = mediaItem)
                songServiceHandler.play()
            } catch (e: Exception) {
                Log.d("PLAYLIST ERROR", "playPlaylist: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun playPlaylistSong(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            try {
                if (playlistId != selectedPlaylist.id) {
                    val playlist = playlistRepo.getPlaylistById(playlistId)
                    val song = playlist.songs.find { it.id == songId }

                    song?.let {
                        _currentSelectedAudio.value = it
                    }
                    _selectedPlaylist.value = playlist
                    songServiceHandler.setMediaItemList(emptyList())
                    val mediaItem = playlist.songs.map { createMediaItem(it) }

                    songServiceHandler.setMediaItemList(mediaItems = mediaItem)
                    songServiceHandler.play()
                } else {
                    val song = selectedPlaylist.songs.find { it.id == songId }
                    song?.let {
                        _currentSelectedAudio.value = it
                    }
                    songServiceHandler.playStreamingAudio(songId)
                }

            } catch (e: Exception) {
                Log.d("PLAYLIST ERROR", "playPlaylist: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setUri("http://${Constants.BASE_URL}/api/song/stream/${song.id}".toUri())
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle(song.title)
                    .setArtist(song.author).setExtras(Bundle().apply {
                        putLong("songId", song.id)
                    }).build()
            ).build()
    }

    fun playSong(songId: Long) {
        viewModelScope.launch {
            try {
                val selectedSong = audioList.find { it.id == songId }
                Log.d("SELECTEDSONG", "playSong: selected song ${selectedSong?.id} ")
                if (selectedSong != null) {
                    _currentSelectedAudio.value = selectedSong

                    val mediaItem = createMediaItem(selectedSong)

                    songServiceHandler.addMediaItem(songId, mediaItem)
                    songServiceHandler.playStreamingAudio(songId)
                }
            } catch (e: Exception) {
                Log.d("PLAYSONGERROR", "playSong: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun onUiEvent(uiEvents: UiEvents) {
        viewModelScope.launch {
            when (uiEvents) {
                UiEvents.Next -> {
                    Log.d("NEXT", "NEXT BUTTON PRESSED")
                    songServiceHandler.onPlayerEvents(PlayerEvent.Next)
                }

                UiEvents.Previous -> {
                    Log.d("PREVIOUS", "PREVIOUS BUTTON PRESSED")
                    songServiceHandler.onPlayerEvents(PlayerEvent.Previous)
                }

                is UiEvents.PlayPause -> {
                    songServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
                    isPlaying = !isPlaying
                    Log.d("AudioViewModel", "isPlaying updated: $isPlaying")
                }

                is UiEvents.SeekTo -> {
                    val seekPositionMs = (duration * uiEvents.position).toLong()
                    Log.d(
                        "SEEKING",
                        "Seeking to position: $seekPositionMs ms (current progress: $progress)"
                    )

                    progress = uiEvents.position * 100f

                    songServiceHandler.onPlayerEvents(
                        PlayerEvent.SeekTo, seekPosition = seekPositionMs
                    )
                }

                is UiEvents.SelectedAudioChange -> playSong(audioList[uiEvents.index].id)
                is UiEvents.SelectedPlaylistChange -> playPlaylist(playlists[uiEvents.index].id)
                is UiEvents.UpdateProgress -> progress = uiEvents.newProgress
            }
        }
    }

    private fun observePlayerEvents() {
        viewModelScope.launch {
            songServiceHandler.audioState.collectLatest { audioState ->
                when (audioState) {
                    AudioState.Initial -> _uiState.value = UiState.Initial
                    is AudioState.Buffer -> calculateProgressValue(audioState.buffer)
                    is AudioState.Current -> {
                        audioState.songId?.let { id ->
                            audioList.find { it.id == id }?.let { song ->
                                _currentSelectedAudio.value = song
                                Log.d("CURRENT_SONG", "Updated to: ${song.title} by ${song.author}")
                            }
                        }
                    }

                    is AudioState.Playing -> {
                        isPlaying = audioState.isPlaying
                        Log.d("AudioViewModel", "isPlaying updated: $isPlaying")
                    }

                    is AudioState.Progress -> calculateProgressValue(audioState.progress)
                    is AudioState.Ready -> {
                        duration = audioState.duration
                        _uiState.value = UiState.Ready
                    }
                }
            }
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress = if (currentProgress > 0) {
            ((currentProgress.toFloat() / duration.toFloat()) * 100f)
        } else 0f
        progressString = formatDuration(currentProgress)
    }

    fun likeSong(id: Long) {
        viewModelScope.launch {
            try {
                if (audioRepo.likeSong(id) == 200) _currentSelectedAudio.value =
                    _currentSelectedAudio.value.copy(isLiked = true)
                _audioList.value = _audioList.value.map { song ->
                    if (song.id == id) song.copy(isLiked = true) else song
                }
            } catch (e: Exception) {
                Log.d("LIKE ERROR", "LIKE ERROR: ${e.message}")
            }
        }
    }

    fun unlikeSong(id: Long) {
        viewModelScope.launch {
            try {
                if (audioRepo.unlikeSong(id) == 200) _currentSelectedAudio.value =
                    _currentSelectedAudio.value.copy(isLiked = false)
                _audioList.value = _audioList.value.map { song ->
                    if (song.id == id) song.copy(isLiked = false) else song
                }
            } catch (e: Exception) {
                Log.d("UNLIKE ERROR", "UNLIKE ERROR: ${e.message}")
            }
        }
    }

    fun resetState() {
        _currentSelectedAudio.value =
            audioDummy.copy(title = "No song selected", author = "Unknown")
        _audioList.value = emptyList()
        _uiState.value = UiState.Initial
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(currentProgress: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(currentProgress)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(currentProgress) % TimeUnit.MINUTES.toSeconds(1)
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun refreshAudioData() {
        viewModelScope.launch {
            try {
                val songs = audioRepo.getAudioData()
                _audioList.value = songs
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


sealed class UiState {
    object Initial : UiState()
    object Ready : UiState()
}

sealed class UiEvents {
    object PlayPause : UiEvents()
    data class SelectedAudioChange(val index: Int) : UiEvents()
    data class SelectedPlaylistChange(val index: Int) : UiEvents()
    data class SeekTo(val position: Float) : UiEvents()
    object Next : UiEvents()
    object Previous : UiEvents()
    data class UpdateProgress(val newProgress: Float) : UiEvents()

}