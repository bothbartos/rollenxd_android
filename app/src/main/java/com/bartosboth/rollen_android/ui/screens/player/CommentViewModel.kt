package com.bartosboth.rollen_android.ui.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.model.comment.Comment
import com.bartosboth.rollen_android.data.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
): ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Idle)
    val commentState = _commentState.asStateFlow()

    fun getComments(songId: Long){
        viewModelScope.launch {
            try{
                _commentState.value = CommentState.Loading
                val response = commentRepository.getCommentsBySongId(songId)
                response.let {
                    _comments.value = it
                    _commentState.value = CommentState.Success
                }
                Log.d("COMMENT_VM", "getComments: ${comments.value.size}")
            }catch (e: Exception){
                _commentState.value = CommentState.Error("Loading error: ${e.message}")
            }
        }
    }

    fun addComment(songId: Long, text: String){
        viewModelScope.launch {
            try{
                _commentState.value = CommentState.Loading
                Log.d("ADD_COMMENT", "addComment: $text")
                val response = commentRepository.addComment(songId, text)
                Log.d("ADD_COMMENT_RESPONSE", "addComment: ${response.text}")
                response.let {
                    _commentState.value = CommentState.Success
                    _comments.value+= response
                }
            }catch (e: Exception){
                _commentState.value = CommentState.Error("Loading error: ${e.message}")
            }
        }
    }

}

sealed class CommentState {
    data object Idle : CommentState()
    data object Loading : CommentState()
    data object Success : CommentState()
    data class Error(val message: String) : CommentState()
}