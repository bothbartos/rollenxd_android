package com.bartosboth.rollen_android

import android.net.Uri
import app.cash.turbine.test
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.data.model.user.UserUpdateDetail
import com.bartosboth.rollen_android.data.repository.UserDetailRepository
import com.bartosboth.rollen_android.ui.screens.main.UpdateState
import com.bartosboth.rollen_android.ui.screens.main.UserDetailViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModelKotestTest : StringSpec({
    lateinit var viewModel: UserDetailViewModel
    lateinit var userDetailRepository: UserDetailRepository
    lateinit var tokenManager: TokenManager
    val testDispatcher = StandardTestDispatcher()

    // Sample test data
    val testUserDetail = UserDetail(
        id = 1L,
        name = "Test User",
        email = "test@example.com",
        bio = "Test bio",
        profileImageBase64 = "base64string",
        songs = emptyList()
    )

    val testUpdateDetail = UserUpdateDetail(
        bio = "Updated bio",
        profilePictureBase64 = "newbase64string"
    )

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        userDetailRepository = mockk()
        tokenManager = mockk()
        viewModel = UserDetailViewModel(userDetailRepository, tokenManager)
    }


    afterTest {
        Dispatchers.resetMain()
    }

    "should load user details when logged in" {
        coEvery { userDetailRepository.getUserDetail() } returns testUserDetail
        val isLoggedInFlow = MutableStateFlow(false)
        coEvery { tokenManager.isLoggedIn } returns isLoggedInFlow

        viewModel = UserDetailViewModel(userDetailRepository, tokenManager)

        // Trigger login
        isLoggedInFlow.value = true
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.userDetails.value shouldBe testUserDetail
        viewModel.bio.value shouldBe testUserDetail.bio
        viewModel.profilePictureBase64.value shouldBe testUserDetail.profileImageBase64

        coVerify(exactly = 2) { userDetailRepository.getUserDetail() }
    }

    "should reset state when logged out" {
        // Setup - start with logged in state
        coEvery { userDetailRepository.getUserDetail() } returns testUserDetail
        val isLoggedInFlow = MutableStateFlow(true)
        every { tokenManager.isLoggedIn } returns isLoggedInFlow

        viewModel = UserDetailViewModel(userDetailRepository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial state
        viewModel.userDetails.value shouldBe testUserDetail

        // Trigger logout
        isLoggedInFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify reset state
        viewModel.userDetails.value shouldBe UserDetail(
            id = 1L,
            name = "",
            email = "",
            bio = "",
            profileImageBase64 = "",
            songs = emptyList()
        )
        viewModel.bio.value shouldBe ""
        viewModel.profilePictureBase64.value shouldBe ""
        viewModel.updateState.value shouldBe UpdateState.Idle
    }

    "should update user details successfully" {
        // Setup
        val testUri = mockk<Uri>()
        coEvery {
            userDetailRepository.updateUserDetail(
                "Updated bio",
                testUri
            )
        } returns testUpdateDetail

        // Execute
        viewModel.updateUserDetails("Updated bio", testUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.updateState.test {
            awaitItem() shouldBe UpdateState.Success
            cancelAndConsumeRemainingEvents()
        }

        viewModel.bio.value shouldBe "Updated bio"
        viewModel.profilePictureBase64.value shouldBe "newbase64string"

        coVerify(exactly = 1) { userDetailRepository.updateUserDetail("Updated bio", testUri) }
    }

    "should handle error during update" {
        // Setup
        val testUri = mockk<Uri>()
        val errorMessage = "Network error"
        coEvery { userDetailRepository.updateUserDetail(any(), any()) } throws Exception(
            errorMessage
        )

        // Execute
        viewModel.updateUserDetails("Updated bio", testUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify
        viewModel.updateState.test {
            val error = awaitItem() as UpdateState.Error
            error.message shouldBe errorMessage
            cancelAndConsumeRemainingEvents()
        }
    }

    "should reset state correctly" {
        // Setup
        viewModel.userDetails.test {
            viewModel = UserDetailViewModel(userDetailRepository, tokenManager)
            testDispatcher.scheduler.advanceUntilIdle()

            // Execute reset
            viewModel.resetState()

            // Verify
            awaitItem() shouldBe UserDetail(
                id = 1L,
                name = "",
                email = "",
                bio = "",
                profileImageBase64 = "",
                songs = emptyList()
            )
            cancelAndConsumeRemainingEvents()
        }

        viewModel.bio.value shouldBe ""
        viewModel.profilePictureBase64.value shouldBe ""
        viewModel.updateState.value shouldBe UpdateState.Idle
    }
})
