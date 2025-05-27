package com.bartosboth.rollen_android

import com.bartosboth.rollen_android.data.model.comment.Comment
import com.bartosboth.rollen_android.data.repository.CommentRepository
import com.bartosboth.rollen_android.ui.screens.player.CommentState
import com.bartosboth.rollen_android.ui.screens.player.CommentViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CommentViewModelKotestTest : StringSpec({
    lateinit var viewModel: CommentViewModel
    lateinit var commentRepository: CommentRepository
    val testDispatcher = StandardTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        commentRepository = mockk()
        viewModel = CommentViewModel(commentRepository)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should load comments successfully" {
        coEvery { commentRepository.getCommentsBySongId(1L) } returns listOf(
            Comment(
                id = 1L,
                songId = 1L,
                userId = 1L,
                username = "test",
                profilePicture = "",
                text = "Comment 1"
            ),
            Comment(
                id = 2L,
                songId = 1L,
                userId = 1L,
                username = "test",
                profilePicture = "",
                text = "Comment 2"
            )
        )
        viewModel.getComments(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.comments.value.size shouldBe 2
        viewModel.comments.value[0].text shouldBe "Comment 1"
        viewModel.commentState.value shouldBe CommentState.Success
    }

    "should handle error when loading comments"{
        coEvery { commentRepository.getCommentsBySongId(1L) } throws Exception("Network error")
        viewModel.getComments(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.commentState.value shouldBe CommentState.Error("Loading error: Network error")
    }

    "should add comment successfully" {
        coEvery { commentRepository.addComment(1L, "New comment") } returns Comment(
            id = 1L,
            songId = 1L,
            userId = 1L,
            username = "test",
            profilePicture = "",
            text = "New comment"
        )
        viewModel.addComment(1L, "New comment")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.comments.value.size shouldBe 1

    }

    "should handle error when adding comment"{
        coEvery { commentRepository.addComment(1L, "New comment") } throws Exception("Network error")
        viewModel.addComment(1L, "New comment")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.commentState.value shouldBe CommentState.Error("Error adding comment: Network error")

    }


})