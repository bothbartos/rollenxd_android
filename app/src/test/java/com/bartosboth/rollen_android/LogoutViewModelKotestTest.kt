package com.bartosboth.rollen_android

import com.bartosboth.rollen_android.data.repository.AuthRepository
import com.bartosboth.rollen_android.ui.screens.profile.AuthState
import com.bartosboth.rollen_android.ui.screens.profile.LogoutViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LogoutViewModelKotestTest : StringSpec({
    lateinit var viewModel: LogoutViewModel
    lateinit var authRepository: AuthRepository
    val testDispatchers = StandardTestDispatcher()

    beforeTest {
        Dispatchers.setMain(testDispatchers)
        authRepository = mockk()
        viewModel = LogoutViewModel(authRepository)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "should emit LoggedOut on successful logout" {
        coEvery { authRepository.logout() } just Runs
        viewModel.logout()
        testDispatchers.scheduler.advanceUntilIdle()
        viewModel.authState.value shouldBe AuthState.LoggedOut


    }
})