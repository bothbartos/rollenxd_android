package com.bartosboth.rollen_android

import app.cash.turbine.test
import com.bartosboth.rollen_android.data.repository.AuthRepository
import com.bartosboth.rollen_android.ui.screens.register.RegisterState
import com.bartosboth.rollen_android.ui.screens.register.RegisterViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelKotestTest : StringSpec({
    lateinit var viewModel: RegisterViewModel
    lateinit var authRepository: AuthRepository
    val testDispatchers = StandardTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatchers)
        authRepository = mockk()
        viewModel = RegisterViewModel(authRepository)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should emit Success on successful registration" {
        val mockResponse: Response<Void> = Response.success(null)
        coEvery { authRepository.register(any(), any(), any()) } returns mockResponse

        viewModel.register("user", "email@email.com", "password")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.registerState.test {
            awaitItem() shouldBe RegisterState.Success
            cancelAndConsumeRemainingEvents()
        }
    }

    "should emit Error with appropriate message on taken username registration" {
        val errorResponse = Response.error<Void>(
            409,
            "Conflict".toResponseBody("text/plain".toMediaType())
        )
        coEvery { authRepository.register(any(), any(), any()) } returns errorResponse

        viewModel.register("user", "email@email.com", "password")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.registerState.test {
            val error = awaitItem() as RegisterState.Error
            error.message shouldBe "Username or email already exists"
            cancelAndConsumeRemainingEvents()
        }
    }

    "should emit Error with appropriate message on invalid input data" {
        val errorResponse = Response.error<Void>(
            400,
            "Bad Request".toResponseBody("text/plain".toMediaType())
        )
        coEvery { authRepository.register(any(), any(), any()) } returns errorResponse

        viewModel.register("user", "email@email.com", "password")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.registerState.test {
            val error = awaitItem() as RegisterState.Error
            error.message shouldBe "Invalid input data"
            cancelAndConsumeRemainingEvents()
        }
    }

    "should emit Network error message on exception" {
        coEvery { authRepository.register(any(), any(), any()) } throws IOException("Network error")

        viewModel.register("user", "email@email.com", "password")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.registerState.test {
            val error = awaitItem() as RegisterState.Error
            error.message shouldBe "Network error: Network error"
            cancelAndConsumeRemainingEvents()
        }
    }
})
