package com.bartosboth.rollen_android

import app.cash.turbine.test
import com.bartosboth.rollen_android.data.model.playlist.Playlist
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.PlaylistRepository
import com.bartosboth.rollen_android.ui.screens.playlistDetail.PlaylistDetailViewModel
import com.bartosboth.rollen_android.ui.screens.playlistDetail.PlaylistState
import com.bartosboth.rollen_android.utils.Constants
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModelKotestTest : StringSpec({
    lateinit var viewModel: PlaylistDetailViewModel
    lateinit var playlistRepository: PlaylistRepository
    val testDispatcher = StandardTestDispatcher()

    // Sample test data
    val testPlaylist = Playlist(
        id = 1L,
        title = "Test Playlist",
        author = "Test Author",
        coverBase64 = "test_cover_base64",
        songs = listOf(
            Song(
                id = 1L,
                title = "Song 1",
                author = "Artist 1",
                coverBase64 = "cover1",
                isLiked = false,
                length = 100.0,
                reShares = 0,
            ),
            Song(
                id = 2L,
                title = "Song 2",
                author = "Artist 2",
                coverBase64 = "cover2",
                isLiked = true,
                length = 120.0,
                reShares = 0,
            )
        )
    )

    val likedSongs = listOf(
        Song(
            id = 3L,
            title = "Liked Song 1",
            author = "Artist 3",
            coverBase64 = "cover3",
            isLiked = false,
            length = 150.0,
            reShares = 0
        ),
        Song(
            id = 4L,
            title = "Liked Song 2",
            author = "Artist 4",
            coverBase64 = "cover4",
            isLiked = false,
            length = 180.0,
            reShares = 0
        )
    )

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        playlistRepository = mockk()
        viewModel = PlaylistDetailViewModel(playlistRepository)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should load regular playlist successfully" {
        // Setup
        coEvery { playlistRepository.getPlaylistById(1L) } returns testPlaylist

        // Execute
        viewModel.getPlaylist(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state transitions
        viewModel.playlistState.test {
            awaitItem() shouldBe PlaylistState.Success
            cancelAndConsumeRemainingEvents()
        }

        // Verify playlist data
        viewModel.playlist.value shouldBe testPlaylist

        // Verify repository was called
        coVerify(exactly = 1) { playlistRepository.getPlaylistById(1L) }
    }

    "should load liked songs playlist successfully" {
        // Setup
        coEvery { playlistRepository.getLikedSongs() } returns likedSongs

        // Execute
        viewModel.getPlaylist(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state transitions
        viewModel.playlistState.test {
            awaitItem() shouldBe PlaylistState.Success
            cancelAndConsumeRemainingEvents()
        }

        // Verify playlist data
        val expectedPlaylist = Playlist(
            id = 0L,
            title = "Liked Songs",
            author = "You",
            coverBase64 = Constants.LIKED_SONG_BASE64,
            songs = likedSongs.map { it.copy(isLiked = true) }
        )

        viewModel.playlist.value.id shouldBe expectedPlaylist.id
        viewModel.playlist.value.title shouldBe expectedPlaylist.title
        viewModel.playlist.value.author shouldBe expectedPlaylist.author
        viewModel.playlist.value.coverBase64 shouldBe expectedPlaylist.coverBase64

        // Verify all songs are marked as liked
        viewModel.playlist.value.songs.forEach {
            it.isLiked shouldBe true
        }

        // Verify repository was called
        coVerify(exactly = 1) { playlistRepository.getLikedSongs() }
    }

    "should handle error when loading regular playlist" {
        // Setup
        val errorMessage = "Network error"
        coEvery { playlistRepository.getPlaylistById(1L) } throws Exception(errorMessage)

        // Execute
        viewModel.getPlaylist(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state transitions
        viewModel.playlistState.test {
            val error = awaitItem() as PlaylistState.Error
            error.message shouldBe "Loading error: $errorMessage"
            cancelAndConsumeRemainingEvents()
        }

        // Verify playlist data remains unchanged
        viewModel.playlist.value shouldBe Playlist(
            id = -1L,
            title = "",
            author = "",
            coverBase64 = "",
            songs = emptyList()
        )

        // Verify repository was called
        coVerify(exactly = 1) { playlistRepository.getPlaylistById(1L) }
    }

    "should handle error when loading liked songs playlist" {
        // Setup
        val errorMessage = "Network error"
        coEvery { playlistRepository.getLikedSongs() } throws Exception(errorMessage)

        // Execute
        viewModel.getPlaylist(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify state transitions
        viewModel.playlistState.test {
            val error = awaitItem() as PlaylistState.Error
            error.message shouldBe "Loading error: $errorMessage"
            cancelAndConsumeRemainingEvents()
        }

        // Verify playlist data remains unchanged
        viewModel.playlist.value shouldBe Playlist(
            id = -1L,
            title = "",
            author = "",
            coverBase64 = "",
            songs = emptyList()
        )

        // Verify repository was called
        coVerify(exactly = 1) { playlistRepository.getLikedSongs() }
    }

    "should have initial idle state" {
        // Verify initial state without any actions
        viewModel.playlistState.value shouldBe PlaylistState.Idle

        viewModel.playlist.value shouldBe Playlist(
            id = -1L,
            title = "",
            author = "",
            coverBase64 = "",
            songs = emptyList()
        )
    }
})
