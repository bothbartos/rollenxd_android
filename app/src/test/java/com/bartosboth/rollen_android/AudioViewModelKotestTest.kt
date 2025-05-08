package com.bartosboth.rollen_android

import androidx.lifecycle.SavedStateHandle
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.playlist.PlaylistData
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.player.service.AudioState
import com.bartosboth.rollen_android.data.player.service.PlayerEvent
import com.bartosboth.rollen_android.data.player.service.SongServiceHandler
import com.bartosboth.rollen_android.data.repository.AudioRepository
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents
import com.bartosboth.rollen_android.ui.screens.audio.UiState
import com.bartosboth.rollen_android.utils.Constants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AudioViewModelKotestTest : StringSpec({
    lateinit var viewModel: AudioViewModel
    lateinit var songServiceHandler: SongServiceHandler
    lateinit var audioRepo: AudioRepository
    lateinit var playlistRepo: PlaylistRepository
    lateinit var tokenManager: TokenManager
    lateinit var savedStateHandle: SavedStateHandle
    val testDispatcher = StandardTestDispatcher()

    // Sample test data
    val testSongs = listOf(
        Song(
            id = 1L,
            title = "Song 1",
            author = "Artist 1",
            coverBase64 = Constants.LIKED_SONG_BASE64,
            isLiked = false,
            length = 100.0,
            reShares = 0,
        ),
        Song(
            id = 2L,
            title = "Song 2",
            author = "Artist 2",
            coverBase64 = Constants.LIKED_SONG_BASE64,
            isLiked = true,
            length = 120.0,
            reShares = 0,
        )
    )

    val testPlaylists = listOf(
        PlaylistData(id = 1L, title = "Playlist 1", author = "User 1", coverBase64 = Constants.LIKED_SONG_BASE64),
        PlaylistData(id = 2L, title = "Playlist 2", author = "User 2", coverBase64 = Constants.LIKED_SONG_BASE64)
    )

    val testPlaylist = Playlist(
        id = 1L,
        title = "Test Playlist",
        author = "Test Author",
        coverBase64 = Constants.LIKED_SONG_BASE64,
        songs = testSongs
    )


    beforeTest {
        Dispatchers.setMain(testDispatcher)
        songServiceHandler = mockk(relaxed = true)
        audioRepo = mockk()
        playlistRepo = mockk()
        tokenManager = mockk()
        savedStateHandle = SavedStateHandle()

        // Mock the StateFlow in TokenManager
        val isLoggedInFlow = MutableStateFlow(false)
        coEvery { tokenManager.isLoggedIn } returns isLoggedInFlow

        // Mock AudioState flow
        val audioStateFlow = MutableStateFlow<AudioState>(AudioState.Initial)
        every { songServiceHandler.audioState } returns audioStateFlow

        viewModel = AudioViewModel(
            songServiceHandler = songServiceHandler,
            audioRepo = audioRepo,
            playlistRepo = playlistRepo,
            tokenManager = tokenManager,
            savedStateHandle = savedStateHandle
        )
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should load audio data when logged in" {
        // Setup
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map { it.copy(isLiked = true) }

        val isLoggedInFlow = MutableStateFlow(false)

        // Trigger login
        coEvery { tokenManager.isLoggedIn } returns isLoggedInFlow
        isLoggedInFlow.value = true

        testDispatcher.scheduler.advanceUntilIdle()


        // Verify
        viewModel.audioList shouldBe testSongs
        viewModel.playlists.size shouldBe testPlaylists.size + 1
        viewModel.playlists[0].title shouldBe "Liked Songs"

        coVerify {
            audioRepo.getAudioData()
            playlistRepo.getPlaylists()
            audioRepo.getLikedSongs()
        }
    }

    "should reset state when logged out" {
        // Setup - start with logged in state
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map { it.copy(isLiked = true) }

        val isLoggedInFlow = MutableStateFlow(true)
        coEvery { tokenManager.isLoggedIn } returns isLoggedInFlow

        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial state
        viewModel.audioList shouldBe testSongs

        // Trigger logout
        isLoggedInFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify reset state
        viewModel.audioList shouldBe emptyList()
        viewModel.uiState.value shouldBe UiState.Initial
        viewModel.currentSelectedAudio.title shouldBe "No song selected"
    }

    "should play playlist successfully" {
        // Setup
        coEvery { playlistRepo.getPlaylistById(1L) } returns testPlaylist

        // Execute
        viewModel.playPlaylist(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.selectedPlaylist shouldBe testPlaylist

        verify {
            songServiceHandler.setMediaItemList(emptyList())
            songServiceHandler.setMediaItemList(any())
        }

        coVerify { playlistRepo.getPlaylistById(1L) }
    }

    "should play liked songs playlist" {
        // Setup
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map { it.copy(isLiked = true) }
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        testDispatcher.scheduler.advanceUntilIdle()

        // Set liked songs
        viewModel.playPlaylist(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.selectedPlaylist.id shouldBe 0L
        viewModel.selectedPlaylist.title shouldBe "Liked Songs"
        viewModel.selectedPlaylist.author shouldBe "You"

        verify {
            songServiceHandler.setMediaItemList(emptyList())
            songServiceHandler.setMediaItemList(any())
            songServiceHandler.play()
        }
    }

    "should play song successfully" {
        // Setup
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map{ it.copy(isLiked = true)}
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        testDispatcher.scheduler.advanceUntilIdle()

        // Execute
        viewModel.playSong(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.currentSelectedAudio.id shouldBe 1L

    }

    "should play playlist song successfully" {
        // Setup
        coEvery { playlistRepo.getPlaylistById(1L) } returns testPlaylist

        // Execute
        viewModel.playPlaylistSong(2L, 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.selectedPlaylist shouldBe testPlaylist

        coVerify { playlistRepo.getPlaylistById(1L) }
    }

    "should handle PlayPause UI event" {
        // Execute
        viewModel.onUiEvent(UiEvents.PlayPause)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        coVerify { songServiceHandler.onPlayerEvents(PlayerEvent.PlayPause) }
        viewModel.isPlaying shouldBe true
    }

    "should handle Next UI event" {
        // Execute
        viewModel.onUiEvent(UiEvents.Next)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        coVerify { songServiceHandler.onPlayerEvents(PlayerEvent.Next) }
    }

    "should handle Previous UI event" {
        // Execute
        viewModel.onUiEvent(UiEvents.Previous)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        coVerify { songServiceHandler.onPlayerEvents(PlayerEvent.Previous) }
    }

    "should handle SeekTo UI event" {
        // Setup
        viewModel.duration = 100000L

        // Execute
        viewModel.onUiEvent(UiEvents.SeekTo(0.5f))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        coVerify { songServiceHandler.onPlayerEvents(PlayerEvent.SeekTo, seekPosition = 50000L) }
        viewModel.progress shouldBe 50f
    }

    "should like song successfully" {
        // Setup
        coEvery { audioRepo.likeSong(1L) } returns 200
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.filter { it.id == 1L }
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        testDispatcher.scheduler.advanceUntilIdle()

        // Set current song
        viewModel.playSong(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Execute
        viewModel.likeSong(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.currentSelectedAudio.isLiked shouldBe true
        viewModel.audioList.find { it.id == 1L }?.isLiked shouldBe true

        coVerify { audioRepo.likeSong(1L) }
    }

    "should unlike song successfully" {
        // Setup
        coEvery { audioRepo.unlikeSong(2L) } returns 200
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.filter { it.id == 2L }.map{ it.copy(isLiked = true)}
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        testDispatcher.scheduler.advanceUntilIdle()

        // Set current song
        viewModel.playSong(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Execute
        viewModel.unlikeSong(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.currentSelectedAudio.isLiked shouldBe false
        viewModel.audioList.find { it.id == 2L }?.isLiked shouldBe false

        coVerify { audioRepo.unlikeSong(2L) }
    }

    "should update progress when audio state changes" {
        // Setup
        val audioStateFlow = MutableStateFlow<AudioState>(AudioState.Initial)
        every { songServiceHandler.audioState } returns audioStateFlow

        // Mock login state and data loading
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map { it.copy(isLiked = true) }
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        // Collect uiState to trigger updates
        val job = launch {
            viewModel.uiState.collect {}
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate Ready state
        audioStateFlow.value = AudioState.Ready(60000L)
        testDispatcher.scheduler.advanceUntilIdle()

        job.cancel()

        // Verify Ready state
        viewModel.duration shouldBe 60000L
        viewModel.uiState.value shouldBe UiState.Ready
        // Verify Progress state
        viewModel.progress shouldBe 0.0f
        viewModel.progressString shouldBe "00:00"

        // Simulate Playing state
        audioStateFlow.value = AudioState.Playing(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify Playing state
        viewModel.isPlaying shouldBe true
    }

    "should update current song when audio state changes" {
        // Setup
        coEvery { audioRepo.getAudioData() } returns testSongs
        coEvery { playlistRepo.getPlaylists() } returns testPlaylists
        coEvery { audioRepo.getLikedSongs() } returns testSongs.map { it.copy(isLiked = true) }
        val audioStateFlow = MutableStateFlow<AudioState>(AudioState.Initial)
        every { songServiceHandler.audioState } returns audioStateFlow
        every { tokenManager.isLoggedIn } returns MutableStateFlow(true)

        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate Current state
        audioStateFlow.value = AudioState.Current(
            mediaItemIndex = 0,
            songId = 1L,
            title = "Song 1",
            artist = "Artist 1"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify Current state
        viewModel.currentSelectedAudio.id shouldBe 1L
        viewModel.currentSelectedAudio.title shouldBe "Song 1"
    }

})
