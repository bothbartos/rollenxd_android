package com.bartosboth.rollen_android.ui.screens.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {
    private val _likedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val likedSongIds = _likedSongIds.asStateFlow()

    init {
        viewModelScope.launch {
            _likedSongIds.value = audioRepository.getLikedSongs().map { it.id }.toSet()
        }
    }

    fun toggleLike(songId: Long) {
        viewModelScope.launch {
            val isLiked = _likedSongIds.value.contains(songId)
            _likedSongIds.value = if (isLiked) {
                _likedSongIds.value - songId
            } else {
                _likedSongIds.value + songId
            }
            if (isLiked) {
                audioRepository.unlikeSong(songId)
            } else {
                audioRepository.likeSong(songId)
            }
        }
    }
}