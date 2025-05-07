package com.bartosboth.rollen_android

import app.cash.turbine.test
import com.bartosboth.rollen_android.data.repository.AuthRepository
import com.bartosboth.rollen_android.ui.screens.login.LoginState
import com.bartosboth.rollen_android.ui.screens.login.LoginViewModel
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
    class LoginViewModelKotestTest : StringSpec({
    lateinit var viewModel: LoginViewModel
    lateinit var authRepository: AuthRepository
    val testDispatchers = StandardTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatchers)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should emit Success on successful login" {
        coEvery { authRepository.login("user", "pass") } returns mockk()

        viewModel.login("user", "pass")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.loginState.test {

            awaitItem() shouldBe LoginState.Success
            cancelAndConsumeRemainingEvents()
        }
    }

    "should emit Error on failed login" {
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.login("user", "wrongpass") } throws Exception(errorMessage)

        viewModel.login("user", "wrongpass")
        testDispatchers.scheduler.advanceUntilIdle()

        viewModel.loginState.test {
            val errorState = awaitItem() as LoginState.Error
            errorState.message shouldBe "Login error: $errorMessage"
            cancelAndConsumeRemainingEvents()
        }
    }
})
