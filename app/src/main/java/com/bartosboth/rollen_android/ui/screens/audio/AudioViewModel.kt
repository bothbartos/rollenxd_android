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
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.AudioRepository
import com.bartosboth.rollen_android.data.player.service.AudioState
import com.bartosboth.rollen_android.data.player.service.PlayerEvent
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
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
    title = "",
    author = "",
    coverBase64 = "",
    length = 0.0,
    isLiked = false,
    reShares = 0,
    id = 1L
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var duration by savedStateHandle.saveable { mutableLongStateOf(1L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }

    private val _currentSelectedAudio =
        mutableStateOf(audioDummy.copy(title = "No song selected", author = "Unknown"))
    val currentSelectedAudio: Song get() = _currentSelectedAudio.value

    private val _audioList = mutableStateOf<List<Song>>(emptyList())
    val audioList: List<Song> get() = _audioList.value

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadAudioData()
        observePlayerEvents()
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            try {
                val songs = repository.getAudioData()
                _audioList.value = songs

                setMediaItems()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setMediaItems() {
        val mediaItems = audioList.map { song ->
            Log.d("SONG_IDS", "setMediaItems: ${song.id}")
            MediaItem.Builder()
                .setUri("http://${Constants.BASE_URL}/api/song/stream/${song.id}".toUri())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.author)
                        .setExtras(Bundle().apply {
                            putLong("songId", song.id)
                        })
                        .build()
                )
                .build()
        }

        songServiceHandler.setMediaItemList(mediaItems)
    }

    fun playSong(songId: Long) {
        viewModelScope.launch {
            try {
                val selectedSong = audioList.find { it.id == songId }
                Log.d("SELECTEDSONG", "playSong: selected song ${selectedSong?.id} ")
                if (selectedSong != null) {
                    _currentSelectedAudio.value = selectedSong
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
                UiEvents.Backward -> songServiceHandler.onPlayerEvents(PlayerEvent.Backward)
                UiEvents.Forward -> songServiceHandler.onPlayerEvents(PlayerEvent.Forward)
                UiEvents.SeekToNext -> songServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
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

                    // Temporarily update UI progress for better responsiveness
                    progress = uiEvents.position * 100f

                    // Perform the actual seek
                    songServiceHandler.onPlayerEvents(
                        PlayerEvent.SeekTo,
                        seekPosition = seekPositionMs
                    )
                }

                is UiEvents.SelectedAudioChange -> playSong(audioList[uiEvents.index].id)
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
                if (repository.likeSong(id) == 200) _currentSelectedAudio.value =
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
                if (repository.unlikeSong(id) == 200) _currentSelectedAudio.value =
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
                val songs = repository.getAudioData()
                _audioList.value = songs
                setMediaItems()
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
    data class SeekTo(val position: Float) : UiEvents()
    object SeekToNext : UiEvents()
    object Backward : UiEvents()
    object Forward : UiEvents()
    data class UpdateProgress(val newProgress: Float) : UiEvents()

}