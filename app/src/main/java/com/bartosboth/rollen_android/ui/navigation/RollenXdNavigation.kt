package com.bartosboth.rollen_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents
import com.bartosboth.rollen_android.ui.screens.login.LoginScreen
import com.bartosboth.rollen_android.ui.screens.login.LoginViewModel
import com.bartosboth.rollen_android.ui.screens.main.MainScreen


@Composable
fun RollenXdNavigation() {
    val navController = rememberNavController()
    val audioViewModel: AudioViewModel = hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()


    NavHost(navController = navController,
        startDestination = if (TokenManager(LocalContext.current).isLoggedIn()) MainScreen else LoginScreen) {
        composable<LoginScreen>{
            LoginScreen(loginViewModel,
                onLoginSuccess = {
                    navController.navigate(MainScreen){
                        popUpTo<LoginScreen>()
                    }
            })
        }

        composable<MainScreen> {
            MainScreen(
                navController,
                progress = audioViewModel.progress,
                onProgressChange = {audioViewModel.onUiEvent(uiEvents = UiEvents.UpdateProgress(it))},
                isAudioPlaying = audioViewModel.isPlaying,
                currentPlayingAudio = audioViewModel.currentSelectedAudio,
                audioList = audioViewModel.audioList,
                onItemClick = {audioViewModel.onUiEvent(UiEvents.SelectedAudioChange(it))},
                onStart = {audioViewModel.onUiEvent(UiEvents.PlayPause)},
                onNext = {audioViewModel.onUiEvent(UiEvents.SeekToNext)},
            )
        }
    }
}
