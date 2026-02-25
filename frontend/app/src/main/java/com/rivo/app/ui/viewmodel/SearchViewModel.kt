package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.ConnectivityRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.SearchRepository
import android.util.Log
import com.rivo.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val musicRepository: MusicRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _musicResults = MutableStateFlow<List<Music>>(emptyList())
    val musicResults: StateFlow<List<Music>> = _musicResults.asStateFlow()

    private val _artistResults = MutableStateFlow<List<User>>(emptyList())
    val artistResults: StateFlow<List<User>> = _artistResults.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _cachedMusic = MutableStateFlow<List<Music>>(emptyList())
    val cachedMusic: StateFlow<List<Music>> = _cachedMusic.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String?>(replay = 0)
    val snackbarMessage: SharedFlow<String?> = _snackbarMessage.asSharedFlow()

    private var lastSearchedQuery: String? = null
    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        observeConnectivity()
        loadCachedMusic()
        loadRecentSearches()

        // Implement debouncing for search query
        viewModelScope.launch {
            _searchQuery
                .debounce(600)
                .filter { it.isNotBlank() && it != lastSearchedQuery }
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityRepository.isNetworkAvailable
                .collectLatest { available ->
                    _isOfflineMode.value = !available
                }
        }
    }

    private fun loadCachedMusic() {
        viewModelScope.launch {
            musicRepository.getAllMusic()
                .collectLatest { list ->
                    _cachedMusic.value = list
                }
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            clearResults()
            searchJob?.cancel()
            lastSearchedQuery = null
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            showSnackbar("Please enter a valid search query")
            clearResults()
            return
        }
        if (query != lastSearchedQuery) {
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query == lastSearchedQuery && !musicResults.value.isEmpty()) return
        
        searchJob?.cancel()
        lastSearchedQuery = query
        _isSearching.value = true
        searchJob = viewModelScope.launch {
            try {
                if (_isOfflineMode.value) {
                    searchOffline(query)
                } else {
                    searchOnline(query)
                }

                saveToRecentSearches(query)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search failed: ${e.message}")
                if (!_isOfflineMode.value) {
                    _isOfflineMode.value = true
                    searchOffline(query)
                    showSnackbar("Network error, switched to offline mode")
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    private suspend fun searchOnline(query: String) {
        val result = searchRepository.searchAll(query)
        result.fold(
            onSuccess = { (music, artists) ->
                _musicResults.value = music
                _artistResults.value = artists
            },
            onFailure = { e ->
                Log.e("SearchViewModel", "Online search failed: ${e.message}")
                searchOffline(query)
            }
        )
    }

    private fun searchOffline(query: String) {
        val q = query.lowercase()
        _musicResults.value = _cachedMusic.value.filter { m ->
            (m.title ?: "").lowercase().contains(q) ||
                    (m.artist ?: "").lowercase().contains(q) ||
                    m.album?.lowercase()?.contains(q) == true ||
                    m.genre?.lowercase()?.contains(q) == true
        }
        _artistResults.value = emptyList() // Artists not usually cached for offline
    }

    private fun saveToRecentSearches(query: String) {
        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserId()
            if (userId.isNotBlank()) {
                searchRepository.saveSearchQuery(userId, query)
                loadRecentSearches()
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserId()
            if (userId.isNotBlank()) {
                _recentSearches.value = searchRepository.getRecentSearches(userId)
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            val userId = sessionManager.getCurrentUserId()
            if (userId.isNotBlank()) {
                searchRepository.clearRecentSearches(userId)
                _recentSearches.value = emptyList()
            }
        }
    }

    fun clearSearch() = clearResults()

    private fun clearResults() {
        _searchQuery.value = ""
        _musicResults.value = emptyList()
        _artistResults.value = emptyList()
        _isSearching.value = false
    }
}