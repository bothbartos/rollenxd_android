
package com.bartosboth.rollen_android.ui.screens.audio

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.playlist.NewPlaylist
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.player.service.AudioState
import com.bartosboth.rollen_android.data.player.service.PlayerEvent
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
import com.bartosboth.rollen_android.data.repository.AudioRepository
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.utils.Constants
import com.bartosboth.rollen_android.utils.convertBase64ToBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
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

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val songServiceHandler: SongServiceHandler,
    private val audioRepo: AudioRepository,
    private val playlistRepo: PlaylistRepository,
    private val tokenManager: TokenManager,
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
        viewModelScope.launch {
            tokenManager.isLoggedIn.collect {
                if (it) {
                    loadAudioData()
                    observePlayerEvents()
                } else {
                    resetState()
                }
            }
        }
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
                    coverBase64 = Constants.LIKED_SONG_BASE64,
                )
                _audioList.value = songs
                _likedSongs.value = likedSongs
                _playlists.value = listOf(likedSongPlaylist) + playlist
                _uiState.value = UiState.Ready
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun playPlaylist(id: Long) {
        viewModelScope.launch {
            try {
                var playlist =
                    when (id) {
                        0L -> Playlist(
                                id = 0L,
                                title = "Liked Songs",
                                author = "You",
                                coverBase64 = Constants.LIKED_SONG_BASE64,
                                songs = likedSongs
                            )
                        else -> playlistRepo.getPlaylistById(id)
                    }
                _selectedPlaylist.value = playlist
                songServiceHandler.setMediaItemList(emptyList())
                val mediaItem = playlist.songs.map { createMediaItem(it) }

                songServiceHandler.setMediaItemList(mediaItems = mediaItem)
                songServiceHandler.play()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun playPlaylistSong(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = when(playlistId) {
                    0L -> Playlist(
                        id = 0L,
                        title = "Liked Songs",
                        author = "You",
                        coverBase64 = Constants.LIKED_SONG_BASE64,
                        songs = likedSongs
                    )
                    else -> {
                        if(_selectedPlaylist.value.id != playlistId){
                            playlistRepo.getPlaylistById(playlistId)
                        } else {
                            _selectedPlaylist.value
                        }
                    }
                }

                _selectedPlaylist.value = playlist
                val mediaItems = playlist.songs.map { createMediaItem(it) }

                songServiceHandler.setMediaItemList(emptyList())

                songServiceHandler.setMediaItemList(mediaItems)

                val songIndex = playlist.songs.indexOfFirst { it.id == songId }
                if (songIndex != -1) {
                    _currentSelectedAudio.value = playlist.songs[songIndex]
                    songServiceHandler.playStreamingAudio(songId)
                }



            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createMediaItem(song: Song): MediaItem {

        val bitmap = convertBase64ToBitmap(song.coverBase64)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val artworkData = outputStream.toByteArray()

        return MediaItem.Builder()
            .setUri("http://${Constants.BASE_URL}/api/song/stream/${song.id}".toUri())
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle(song.title)
                    .setArtist(song.author)
                    .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .setExtras(Bundle().apply {
                        putLong("songId", song.id)
                    }).build()
            ).build()
    }

    fun playSong(songId: Long) {
        viewModelScope.launch {
            try {
                _selectedPlaylist.value = playlistDummy
                val selectedSong = audioList.find { it.id == songId }
                if (selectedSong != null) {
                    _currentSelectedAudio.value = selectedSong

                    val mediaItem = createMediaItem(selectedSong)

                    songServiceHandler.addMediaItem(songId, mediaItem)
                    songServiceHandler.playStreamingAudio(songId)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun uploadSong(title: String, audioFile: Uri?, coverImage: Uri?) {
        viewModelScope.launch {
            try{
                val response = audioRepo.uploadSong(title, audioFile, coverImage)
                if(response == 200){
                    loadAudioData()
                }
            }catch (e: Exception){
                Log.d("UPLOAD_ERR", "uploadSong: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createPlaylist(title: String, songId: List<Long>){
        viewModelScope.launch {
            try{
                val response = playlistRepo.createPlaylist(NewPlaylist(title, songId))
                if(response == 200){
                    loadAudioData()
                }
            }catch (e: Exception){
                Log.d("UPLOAD_ERR", "uploadSong: ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }

    }

    fun onUiEvent(uiEvents: UiEvents) {
        viewModelScope.launch {
            when (uiEvents) {
                UiEvents.Next -> {
                    songServiceHandler.onPlayerEvents(PlayerEvent.Next)
                }

                UiEvents.Previous -> {
                    songServiceHandler.onPlayerEvents(PlayerEvent.Previous)
                }

                is UiEvents.PlayPause -> {
                    songServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
                    isPlaying = !isPlaying
                }

                is UiEvents.SeekTo -> {
                    val seekPositionMs = (duration * uiEvents.position).toLong()
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
                            }
                        }
                    }

                    is AudioState.Playing -> {
                        isPlaying = audioState.isPlaying
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

}


sealed class UiState {
    object Initial : UiState()
    object Ready : UiState()
    data class Error(val message: String) : UiState()
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