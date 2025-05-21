package com.bartosboth.rollen_android.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bartosboth.rollen_android.data.model.song.Song
import com.bartosboth.rollen_android.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<List<Song>>(emptyList())
    val searchResult = _searchResult.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.isNotEmpty()  }
                .distinctUntilChanged()
                .collectLatest {query ->
                    searchSongs(query)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if(query.isEmpty()){
            _searchResult.value = emptyList()
            _searchState.value = SearchState.Idle
        }
    }

     private fun searchSongs(search: String) {
         viewModelScope.launch {
             try{
                 _searchState.value = SearchState.Loading
                 Log.d("SEARCH", "searchSongs: $search")
                 val response = searchRepository.searchSongs(search)
                 response.let{
                     _searchResult.value = it
                     _searchState.value = SearchState.Success
                 }
             }catch (e: Exception){
                 Log.d("SEARCH_ERR", "searchSongs: error:${e.message}")
                 _searchState.value = SearchState.Error("Error: ${e.message}")
             }

         }

    }
}

sealed class SearchState() {
    object Idle : SearchState()
    object Loading : SearchState()
    object Success : SearchState()
    data class Error(val message: String) : SearchState()
}