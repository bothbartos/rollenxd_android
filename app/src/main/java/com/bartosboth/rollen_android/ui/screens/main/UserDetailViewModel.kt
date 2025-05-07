package com.bartosboth.rollen_android.ui.screens.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.manager.TokenManager
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.data.repository.UserDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val userDetailRepository: UserDetailRepository,
    private val tokenManager: TokenManager
): ViewModel(){
    private val _userDetails = MutableStateFlow(userDetailDummy)
    val userDetails: StateFlow<UserDetail> = _userDetails.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _profilePictureBase64 = MutableStateFlow("")
    val profilePictureBase64: StateFlow<String> = _profilePictureBase64.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()


    init{
        viewModelScope.launch {
            tokenManager.isLoggedIn.collect {
                if(it) loadUserDetails()
                else resetState()
            }
        }
    }


    private fun loadUserDetails(){
        viewModelScope.launch {
            try{
                val userDetail = userDetailRepository.getUserDetail()
                _userDetails.value = userDetail
                _bio.value = userDetail.bio
                _profilePictureBase64.value = userDetail.profileImageBase64
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun updateUserDetails(bio: String, profilePictureUri: Uri?) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                Log.d("UserDetailViewModel", "Updating user details - Bio: $bio, Image URI: $profilePictureUri")

                val updatedUser = userDetailRepository.updateUserDetail(bio, profilePictureUri)

                _userDetails.value = _userDetails.value.copy(
                    bio = updatedUser.bio,
                    profileImageBase64 = updatedUser.profilePictureBase64
                )
                _bio.value = updatedUser.bio
                _profilePictureBase64.value = updatedUser.profilePictureBase64
                _updateState.value = UpdateState.Success
                Log.d("UserDetailViewModel", "Update successful")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error updating user details", e)
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState(){
        _userDetails.value = userDetailDummy
        _bio.value = ""
        _profilePictureBase64.value = ""
        _updateState.value = UpdateState.Idle
    }

    fun refreshDetails() {
        viewModelScope.launch {
            try {
                val userDetail = userDetailRepository.getUserDetail()
                _userDetails.value = userDetail
                _bio.value = userDetail.bio
                _profilePictureBase64.value = userDetail.profileImageBase64
                Log.d("UserDetailViewModel", "Refresh successful")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error refreshing user details", e)
            }
        }
    }

}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}