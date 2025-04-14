package com.bartosboth.rollen_android.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.model.UserDetail
import com.bartosboth.rollen_android.data.repository.UserDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val userDetailDummy = UserDetail(
    id = 1L,
    name = "",
    email = "",
    bio = "",
    profileImageBase64 = "",
    songs = emptyList()
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userDetailRepository: UserDetailRepository
): ViewModel(){
    private val _userDetails = MutableStateFlow<UserDetail>(userDetailDummy)
    val userDetails: StateFlow<UserDetail> = _userDetails

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio

    private val _profileImageBase64 = MutableStateFlow("")
    val profileImageBase64: StateFlow<String> = _profileImageBase64

    init{
        loadUserDetails()
    }


    private fun loadUserDetails(){
        viewModelScope.launch {
            try{
                val userDetail = userDetailRepository.getUserDetail()
                _userDetails.value = userDetail
                _bio.value = userDetail.bio
                _profileImageBase64.value = userDetail.profileImageBase64
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}