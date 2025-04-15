package com.bartosboth.rollen_android.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bartosboth.rollen_android.data.manager.DataRefreshCoordinator
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents
import com.bartosboth.rollen_android.ui.screens.audio.UiState
import com.bartosboth.rollen_android.ui.screens.login.LoginScreen
import com.bartosboth.rollen_android.ui.screens.login.LoginViewModel
import com.bartosboth.rollen_android.ui.screens.main.AuthState
import com.bartosboth.rollen_android.ui.screens.main.LogoutViewModel
import com.bartosboth.rollen_android.ui.screens.main.MainScreen
import com.bartosboth.rollen_android.ui.screens.main.UserDetailViewModel
import com.bartosboth.rollen_android.ui.screens.player.PlayerScreen
import com.bartosboth.rollen_android.ui.screens.profile.ProfileScreen
import com.bartosboth.rollen_android.ui.screens.register.RegisterScreen
import com.bartosboth.rollen_android.ui.screens.register.RegisterViewModel


@Composable
fun RollenXdNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val audioViewModel: AudioViewModel = hiltViewModel()
    val userDetailViewModel: UserDetailViewModel = hiltViewModel()
    val startDestination = remember {
        if (TokenManager(context).isLoggedIn()) MainScreen else LoginScreen
    }
    val dataRefreshCoordinator = remember {
        DataRefreshCoordinator(userDetailViewModel, audioViewModel)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginScreen> {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                loginViewModel,
                onNavigateToRegister = { navController.navigate(RegisterScreen) },
                onLoginSuccess = {
                    dataRefreshCoordinator.refresh()
                    navController.navigate(MainScreen) {
                        popUpTo(LoginScreen) { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterScreen> {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(LoginScreen) {
                        popUpTo(RegisterScreen) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<MainScreen> {
            val userDetails by userDetailViewModel.userDetails.collectAsStateWithLifecycle()

            MainScreen(
                userDetail = userDetails,
                navController = navController,
                progress = audioViewModel.progress,
                isAudioPlaying = audioViewModel.isPlaying,
                currentPlayingAudio = audioViewModel.currentSelectedAudio,
                audioList = audioViewModel.audioList,
                onItemClick = { audioViewModel.onUiEvent(UiEvents.SelectedAudioChange(it)) },
                onStart = { audioViewModel.onUiEvent(UiEvents.PlayPause) },
                onLike = { if (audioViewModel.currentSelectedAudio.isLiked) {
                    audioViewModel.unlikeSong(audioViewModel.currentSelectedAudio.id)
                } else {
                    audioViewModel.likeSong(audioViewModel.currentSelectedAudio.id)
                }},
                uiState = audioViewModel.uiState.collectAsState().value
            )
        }

        composable<PlayerScreen> {
            PlayerScreen(
                navController = navController,
                viewModel = audioViewModel
            )
        }

        composable<ProfileScreen> {
            val logoutViewModel: LogoutViewModel = hiltViewModel()
            val authState by logoutViewModel.authState.collectAsState()
            val userDetails by userDetailViewModel.userDetails.collectAsState()
            val updateState by userDetailViewModel.updateState.collectAsState()

            LaunchedEffect(authState) {
                if (authState is AuthState.LoggedOut) {
                    if(audioViewModel.isPlaying) audioViewModel.onUiEvent(UiEvents.PlayPause)
                    audioViewModel.resetState()
                    userDetailViewModel.resetState()
                    navController.navigate(LoginScreen) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            ProfileScreen(
                userDetail = userDetails,
                updateState = updateState,
                onProfileUpdate = { bio, profilePictureBase64 ->
                    Log.d("PROFILESCREEN", "RollenXdNavigation: $bio")
                    userDetailViewModel.updateUserDetails(bio, profilePictureBase64)
                },
                logoutViewModel = logoutViewModel,
                navController = navController
            )
        }
    }
}
