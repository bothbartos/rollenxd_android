package com.bartosboth.rollen_android.ui.screens.main

import androidx.lifecycle.ViewModel
import com.bartosboth.rollen_android.data.repository.UserDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userDetailRepository: UserDetailRepository
): ViewModel(){

}