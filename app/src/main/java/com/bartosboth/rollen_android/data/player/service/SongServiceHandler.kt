package com.bartosboth.rollen_android.data.player.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bartosboth.rollen_android.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
    @ApplicationContext private val context: Context
) : Player.Listener {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private val mediaItemsMap = mutableMapOf<Long, MediaItem>()

    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    fun play(){
        startMediaService()
        exoPlayer.play()

    }

    private fun startMediaService() {
        val intent = Intent(context, SongService::class.java)
        context.startForegroundService(intent)
        Log.d("AudioViewModel", "Starting media service")
    }

    fun addMediaItem(songId: Long, mediaItem: MediaItem) {
        mediaItemsMap[songId] = mediaItem

        if (findMediaItemIndex(songId) == -1) {
            exoPlayer.addMediaItem(mediaItem)
            Log.d("SongServiceHandler", "Added media item to playlist, count: ${exoPlayer.mediaItemCount}")
        }
        if (exoPlayer.mediaItemCount == 0) {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            play()
        } else {
            val currentSongId = exoPlayer.currentMediaItem?.mediaMetadata?.extras?.getLong("songId")
            if (currentSongId == songId) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            } else {
                if (!mediaItemsMap.containsKey(songId)) {
                    exoPlayer.addMediaItem(mediaItem)
                }

                val index = findMediaItemIndex(songId)
                if (index != -1) {
                    exoPlayer.seekTo(index, 0)
                    exoPlayer.prepare()
                    play()
                }
            }
        }
    }

    private fun findMediaItemIndex(songId: Long): Int {
        for (i in 0 until exoPlayer.mediaItemCount) {
            val item = exoPlayer.getMediaItemAt(i)
            val id = item.mediaMetadata.extras?.getLong("songId")
            if (id == songId) {
                return i
            }
        }
        return -1
    }

    @OptIn(UnstableApi::class)
    fun playStreamingAudio(songId: Long) {
        try {
            if (mediaItemsMap.containsKey(songId)) {
                val index = findMediaItemIndex(songId)
                if (index != -1) {
                    exoPlayer.seekTo(index, 0)
                    exoPlayer.prepare()
                    play()
                    return
                }
            }

            val audioUri = "http://${Constants.BASE_URL}/api/song/stream/$songId".toUri()
            Log.d("SongServiceHandler", "Attempting to stream from: $audioUri")

            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(audioUri))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            play()
        } catch (e: Exception) {
            Log.e("SongServiceHandler", "Error streaming audio: ${e.message}", e)
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0
    ) {
        when (playerEvent) {
            PlayerEvent.Next -> {
                Log.d("SongServiceHandler", "Next button pressed, hasNext: ${exoPlayer.hasNextMediaItem()}")
                Log.d("SongServiceHandler", "isCommandAvailable(NEXT): ${exoPlayer.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)}")

                if (exoPlayer.hasNextMediaItem() && exoPlayer.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)) {
                    exoPlayer.seekToNextMediaItem()
                    val metadata = exoPlayer.currentMediaItem?.mediaMetadata
                    val extras = metadata?.extras
                    val songId = extras?.getLong("songId")
                    _audioState.value = AudioState.Current(
                        mediaItemIndex = exoPlayer.currentMediaItemIndex,
                        songId = songId,
                        title = metadata?.title?.toString(),
                        artist = metadata?.artist?.toString()
                    )
                }
            }
            PlayerEvent.Previous -> {
                Log.d("SongServiceHandler", "Previous button pressed")
                if (exoPlayer.hasPreviousMediaItem()) {
                    Log.d("SongServiceHandler", "Previous button pressed, playing previous song")
                    exoPlayer.seekToPreviousMediaItem()
                    val metadata = exoPlayer.currentMediaItem?.mediaMetadata
                    val extras = metadata?.extras
                    val songId = extras?.getLong("songId")
                    _audioState.value = AudioState.Current(
                        mediaItemIndex = exoPlayer.currentMediaItemIndex,
                        songId = songId,
                        title = metadata?.title?.toString(),
                        artist = metadata?.artist?.toString()
                    )
                }
            }
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.SeekTo -> {
                Log.d("SongServiceHandler", "Seeking to position: $seekPosition")
                try {

                        exoPlayer.seekTo(seekPosition)
                        exoPlayer.seekTo(seekPosition)
                } catch (e: Exception) {
                    Log.e("SongServiceHandler", "Error during seek: ${e.message}", e)

                    exoPlayer.seekTo(seekPosition)
                }
                _audioState.value = AudioState.Progress(seekPosition)
            }
            PlayerEvent.SelectedAudioChange -> {
                if (selectedAudioIndex != -1) {
                    exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                    exoPlayer.playWhenReady = true
                }
            }

            PlayerEvent.Stop -> exoPlayer.stop()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> _audioState.value =
                AudioState.Buffer(exoPlayer.currentPosition)

            Player.STATE_READY -> _audioState.value = AudioState.Ready(exoPlayer.duration)
            Player.STATE_ENDED -> _audioState.value = AudioState.Playing(isPlaying = false)
            Player.STATE_IDLE -> _audioState.value = AudioState.Initial
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d("SongServiceHandler", "onIsPlayingChanged: $isPlaying")
        _audioState.value = AudioState.Playing(isPlaying = isPlaying)
        val currentItem = exoPlayer.currentMediaItem
        val songId = currentItem?.mediaMetadata?.extras?.getLong("songId")

        _audioState.value = AudioState.Current(
            mediaItemIndex = exoPlayer.currentMediaItemIndex,
            songId = songId
        )
        if (isPlaying) {
            serviceScope.launch { startProgressUpdate() }
        } else {
            serviceScope.launch { stopProgressUpdate() }
        }

    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        Log.d("SongServiceHandler", "onMediaItemTransition: reason=$reason")

        val metadata = exoPlayer.currentMediaItem?.mediaMetadata
        val extras = metadata?.extras
        val songId = extras?.getLong("songId")

        _audioState.value = AudioState.Current(
            mediaItemIndex = exoPlayer.currentMediaItemIndex,
            songId = songId,
            title = metadata?.title?.toString(),
            artist = metadata?.artist?.toString()
        )
    }

    private suspend fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            startProgressUpdate()
        }
    }

    private suspend fun startProgressUpdate() {
        job?.cancel()
        job = serviceScope.launch {
            while (exoPlayer.isPlaying) {
                _audioState.value = AudioState.Progress(exoPlayer.currentPosition)
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = AudioState.Playing(isPlaying = false)
    }

    fun release() {
        serviceScope.cancel()
        exoPlayer.removeListener(this)
        exoPlayer.release()
    }
}

sealed class AudioState {
    object Initial : AudioState()
    data class Ready(val duration: Long) : AudioState()
    data class Progress(val progress: Long) : AudioState()
    data class Buffer(val buffer: Long) : AudioState()
    data class Playing(val isPlaying: Boolean) : AudioState()
    data class Current(
        val mediaItemIndex: Int,
        val songId: Long? = null,
        val title: String? = null,
        val artist: String? = null
    ) : AudioState()
}

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object SelectedAudioChange : PlayerEvent()
    object Next : PlayerEvent()
    object Previous : PlayerEvent()
    object SeekTo : PlayerEvent()
    object Stop : PlayerEvent()
}
