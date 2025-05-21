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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.audio.LikeViewModel
import com.bartosboth.rollen_android.ui.screens.audio.UiEvents
import com.bartosboth.rollen_android.ui.screens.login.LoginScreen
import com.bartosboth.rollen_android.ui.screens.login.LoginViewModel
import com.bartosboth.rollen_android.ui.screens.main.MainScreen
import com.bartosboth.rollen_android.ui.screens.main.UserDetailViewModel
import com.bartosboth.rollen_android.ui.screens.player.PlayerScreen
import com.bartosboth.rollen_android.ui.screens.playlistDetail.PlaylistDetailScreen
import com.bartosboth.rollen_android.ui.screens.playlistDetail.PlaylistDetailViewModel
import com.bartosboth.rollen_android.ui.screens.profile.AuthState
import com.bartosboth.rollen_android.ui.screens.profile.LogoutViewModel
import com.bartosboth.rollen_android.ui.screens.profile.ProfileScreen
import com.bartosboth.rollen_android.ui.screens.register.RegisterScreen
import com.bartosboth.rollen_android.ui.screens.register.RegisterViewModel
import com.bartosboth.rollen_android.ui.screens.search.SearchScreen
import com.bartosboth.rollen_android.ui.screens.search.SearchViewModel


@Composable
fun RollenXdNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = remember {
        if (TokenManager(context).isLoggedIn()) MainFlow else LoginScreen
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginScreen> {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                loginViewModel,
                onNavigateToRegister = { navController.navigate(RegisterScreen) },
                onLoginSuccess = {
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

        navigation(startDestination = MainScreen, route = MainFlow::class) {
            composable<MainScreen> {backstackEntry ->
                val parentEntry = remember(backstackEntry) {
                    navController.getBackStackEntry(MainFlow)
                }
                val audioViewModel: AudioViewModel = hiltViewModel(parentEntry)
                val userDetailViewModel: UserDetailViewModel = hiltViewModel(parentEntry)
                val likeViewModel: LikeViewModel = hiltViewModel(parentEntry)
                val likedSongIds = likeViewModel.likedSongIds.collectAsState()

                val userDetails by userDetailViewModel.userDetails.collectAsStateWithLifecycle()


                MainScreen(
                    userDetail = userDetails,
                    navController = navController,
                    progress = audioViewModel.progress,
                    isAudioPlaying = audioViewModel.isPlaying,
                    currentPlayingAudio = audioViewModel.currentSelectedAudio,
                    currentPlayingPlaylist = audioViewModel.selectedPlaylist,
                    audioList = audioViewModel.audioList,
                    playlists = audioViewModel.playlists,
                    onSongClick = { audioViewModel.onUiEvent(UiEvents.SelectedAudioChange(it)) },
                    onPlaylistClick = { navController.navigate(PlaylistDetailScreen(playlistId = it)) },
                    onStart = { audioViewModel.onUiEvent(UiEvents.PlayPause) },
                    onLike = { likeViewModel.toggleLike(audioViewModel.currentSelectedAudio.id) },
                    uiState = audioViewModel.uiState.collectAsState().value,
                    isLiked = likedSongIds.value.contains(audioViewModel.currentSelectedAudio.id)
                )
            }

            composable<PlaylistDetailScreen> { backstackEntry ->
                val playlistId =
                    backstackEntry.arguments?.getLong(PlaylistDetailScreen.PLAYLIST_ID_ARG) ?: -1L
                val parentEntry = remember(backstackEntry) {
                    navController.getBackStackEntry(MainFlow)
                }
                val audioViewModel: AudioViewModel = hiltViewModel(parentEntry)
                val playlistViewModel: PlaylistDetailViewModel = hiltViewModel(parentEntry)
                val userDetailViewModel: UserDetailViewModel = hiltViewModel(parentEntry)
                val likeViewModel: LikeViewModel = hiltViewModel(parentEntry)

                val playlist = playlistViewModel.playlist.collectAsState().value
                val playlistState = playlistViewModel.playlistState.collectAsState().value
                val userDetails by userDetailViewModel.userDetails.collectAsStateWithLifecycle()
                val likedSongIds = likeViewModel.likedSongIds.collectAsState()


                LaunchedEffect(playlistId) {
                    if (playlistId != -1L) {
                        playlistViewModel.getPlaylist(playlistId)
                    }
                }

                PlaylistDetailScreen(
                    playlist = playlist,
                    playlistState = playlistState,
                    onBackClick = { navController.popBackStack() },
                    progress = audioViewModel.progress,
                    isAudioPlaying = audioViewModel.isPlaying,
                    currentPlayingAudio = audioViewModel.currentSelectedAudio,
                    onCurrentSongLike = { likeViewModel.toggleLike(audioViewModel.currentSelectedAudio.id) },
                    onSongLike = { likeViewModel.toggleLike(it.id) },
                    playPlaylist = {
                        if (audioViewModel.selectedPlaylist.id == it) audioViewModel
                            .onUiEvent(UiEvents.PlayPause)
                        else audioViewModel.playPlaylist(it)
                    },
                    userDetail = userDetails,
                    onStart = { audioViewModel.onUiEvent(UiEvents.PlayPause) },
                    onPlaylistSongPlay = { songId, playlistId ->
                        audioViewModel.playPlaylistSong(songId, playlistId)
                    },
                    navController = navController,
                    isCurrentSongLiked = likedSongIds.value.contains(audioViewModel.currentSelectedAudio.id),
                    likedSongIds = likedSongIds.value
                )

            }

            composable<PlayerScreen> {backstackEntry ->
                val parentEntry = remember(backstackEntry) {
                    navController.getBackStackEntry(MainFlow)
                }
                val audioViewModel: AudioViewModel = hiltViewModel(parentEntry)
                val likeViewModel: LikeViewModel = hiltViewModel(parentEntry)

                val likedSongIds = likeViewModel.likedSongIds.collectAsState()

                PlayerScreen(
                    navController = navController,
                    coverBase64 = audioViewModel.currentSelectedAudio.coverBase64,
                    songId = audioViewModel.currentSelectedAudio.id,
                    title = audioViewModel.currentSelectedAudio.title,
                    author = audioViewModel.currentSelectedAudio.author,
                    isLiked = likedSongIds.value.contains(audioViewModel.currentSelectedAudio.id),
                    isPlaying = audioViewModel.isPlaying,
                    progress = audioViewModel.progress,
                    progressString = audioViewModel.progressString,
                    duration = audioViewModel.duration,
                    totalDuration = audioViewModel.currentSelectedAudio.length,
                    onLike = { likeViewModel.toggleLike(audioViewModel.currentSelectedAudio.id) },
                    onSeek = { audioViewModel.onUiEvent(UiEvents.SeekTo(it)) },
                    onPrevious = { audioViewModel.onUiEvent(UiEvents.Previous) },
                    onPlayPause = { audioViewModel.onUiEvent(UiEvents.PlayPause) },
                    onNext = { audioViewModel.onUiEvent(UiEvents.Next) },
                )
            }

            composable<SearchScreen> {backstackEntry ->
                val parentEntry = remember(backstackEntry) {
                    navController.getBackStackEntry(MainFlow)
                }

                val audioViewModel: AudioViewModel = hiltViewModel(parentEntry)
                val userDetailViewModel: UserDetailViewModel = hiltViewModel(parentEntry)
                val likeViewModel: LikeViewModel = hiltViewModel(parentEntry)
                val searchViewModel: SearchViewModel = hiltViewModel(parentEntry)

                val userDetails by userDetailViewModel.userDetails.collectAsState()
                val likedSongIds = likeViewModel.likedSongIds.collectAsState()
                val searchState = searchViewModel.searchState.collectAsState().value
                val searchResult = searchViewModel.searchResult.collectAsState().value

                SearchScreen(
                    searchState = searchState,
                    onTextChange = { searchViewModel.updateSearchQuery(it) },
                    searchResult = searchResult,
                    navController = navController,
                    onBackClick = navController::popBackStack,
                    progress = audioViewModel.progress,
                    isAudioPlaying = audioViewModel.isPlaying,
                    currentPlayingAudio = audioViewModel.currentSelectedAudio,
                    onCurrentSongLike = { likeViewModel.toggleLike(audioViewModel.currentSelectedAudio.id) },
                    userDetail = userDetails,
                    onStart = { audioViewModel.onUiEvent(UiEvents.PlayPause) },
                    isCurrentSongLiked = likedSongIds.value.contains(audioViewModel.currentSelectedAudio.id),
                    onSongClick = { audioViewModel.playSong(it) },
                )

            }

            composable<ProfileScreen> {backstackEntry ->
                val parentEntry = remember(backstackEntry) {
                    navController.getBackStackEntry(MainFlow)
                }

                val userDetailViewModel: UserDetailViewModel = hiltViewModel(parentEntry)
                val audioViewModel: AudioViewModel = hiltViewModel(parentEntry)
                val logoutViewModel: LogoutViewModel = hiltViewModel(parentEntry)
                val authState by logoutViewModel.authState.collectAsState()
                val userDetails by userDetailViewModel.userDetails.collectAsState()
                val updateState by userDetailViewModel.updateState.collectAsState()

                LaunchedEffect(authState) {
                    if (authState is AuthState.LoggedOut) {
                        if (audioViewModel.isPlaying) audioViewModel.onUiEvent(UiEvents.PlayPause)
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
}
