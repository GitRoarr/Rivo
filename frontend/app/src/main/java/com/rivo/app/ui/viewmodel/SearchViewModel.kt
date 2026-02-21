package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.ConnectivityRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val musicRepository: MusicRepository,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private var userId: String? = null

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

    init {
        observeConnectivity()
        loadCachedMusic()
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    fun onSnackbarShown() {
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

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            showSnackbar("Please enter a valid search query")
            clearResults()
            return
        }

        _isSearching.value = true
        viewModelScope.launch {
            try {
                if (_isOfflineMode.value) {
                    searchOffline(query)
                } else {
                    searchOnline(query)
                }
            } catch (_: Throwable) {
                // fallback to offline mode if network fails
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
        searchRepository.searchAll(query)
            .firstOrNull()
            ?.let { (music, artists) ->
                _musicResults.value = music
                _artistResults.value = artists
            }

        userId?.let { id ->
            searchRepository.saveSearchQuery(id, query)
            loadRecentSearches()
        }
    }

    private fun searchOffline(query: String) {
        val q = query.lowercase()
        _musicResults.value = _cachedMusic.value.filter { m ->
            m.title.lowercase().contains(q) ||
                    m.artist.lowercase().contains(q) ||
                    m.album?.lowercase()?.contains(q) == true ||
                    m.genre?.lowercase()?.contains(q) == true
        }
        _artistResults.value = emptyList()

        viewModelScope.launch {
            userId?.let { id ->
                searchRepository.saveSearchQuery(id, query)
                loadRecentSearches()
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            userId?.let { id ->
                _recentSearches.value = searchRepository.getRecentSearches(id)
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            userId?.let { id ->
                searchRepository.clearRecentSearches(id)
                _recentSearches.value = emptyList()
            }
        }
    }

    fun clearSearch() = clearResults()

    private fun clearResults() {
        _searchQuery.value = ""
        _musicResults.value = emptyList()
        _artistResults.value = emptyList()
    }


}