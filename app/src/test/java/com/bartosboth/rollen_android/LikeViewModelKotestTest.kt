package com.bartosboth.rollen_android

import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.AudioRepository
import com.bartosboth.rollen_android.ui.screens.audio.LikeViewModel
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
class LikeViewModelKotestTest : StringSpec({
    lateinit var viewModel: LikeViewModel
    lateinit var audioRepository: AudioRepository
    val testDispatcher = StandardTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        audioRepository = mockk()

        coEvery { audioRepository.getLikedSongs() } returns emptyList()
        coEvery { audioRepository.likeSong(any()) } returns 200
        coEvery { audioRepository.unlikeSong(any()) } returns 200

        viewModel = LikeViewModel(audioRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
    afterTest {
        Dispatchers.resetMain()
    }

    val testSongs = listOf(
        Song(
            title = "Song 1",
            author = "User",
            coverBase64 = "",
            length = 10.0,
            isLiked = false,
            reShares = 0,
            id = 1L
        ),
        Song(
            title = "Song 2",
            author = "User",
            coverBase64 = "",
            length = 10.0,
            isLiked = false,
            reShares = 0,
            id = 2L
        )
    )

    "should toggle like to true for a song" {
        viewModel.toggleLike(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.likedSongIds.value shouldBe setOf(1L)
        coVerify { audioRepository.likeSong(1L) }
    }

    "should toggle like to false for a song" {
        viewModel.toggleLike(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.likedSongIds.value shouldBe setOf(1L)

        viewModel.toggleLike(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.likedSongIds.value shouldBe emptySet()
        coVerify { audioRepository.unlikeSong(1L) }
    }
})
