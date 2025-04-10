@file:OptIn(SavedStateHandleSaveableApi::class)

package com.bartosboth.rollen_android.ui.screens.audio

import android.annotation.SuppressLint
import android.net.Uri
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
    uri = "".toUri(),
    author = "",
    length = 0.0,
    title = "",
    numberOfLikes = 0,
    reShares = 0,
    id=2
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
    var currentSelectedAudio by savedStateHandle.saveable {
        mutableStateOf(audioDummy.copy(title = "No song selected", author = "Unknown"))
    }
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<Song>()) }

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
                audioList = songs

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
                .setUri(Uri.parse("http://${Constants.BASE_URL}/api/song/stream/${song.id}"))
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
                    currentSelectedAudio = selectedSong
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
                is UiEvents.SeekTo -> songServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
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
                                currentSelectedAudio = song
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

    @SuppressLint("DefaultLocale")
    private fun formatDuration(currentProgress: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(currentProgress)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(currentProgress) % TimeUnit.MINUTES.toSeconds(1)
        return String.format("%02d:%02d", minutes, seconds)
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