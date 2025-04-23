package com.bartosboth.rollen_android.data.manager

import com.bartosboth.rollen_android.ui.screens.audio.AudioViewModel
import com.bartosboth.rollen_android.ui.screens.main.UserDetailViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRefreshCoordinator @Inject constructor(
    private val userDetailViewModel: UserDetailViewModel,
    private val audioViewModel: AudioViewModel
) {

    fun refresh(){
        userDetailViewModel.refreshDetails()
        audioViewModel.refreshAudioData()
    }
}