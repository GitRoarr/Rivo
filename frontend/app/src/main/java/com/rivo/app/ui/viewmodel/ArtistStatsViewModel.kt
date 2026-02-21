package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.repository.ArtistStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ArtistStatsViewModel @Inject constructor(
    private val artistStatsRepository: ArtistStatsRepository
) : ViewModel() {

    private val _analytics = MutableStateFlow<ArtistAnalytics?>(null)
    val analytics: StateFlow<ArtistAnalytics?> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Fetch analytics as a one-time operation
    fun loadAnalytics(artistId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = artistStatsRepository.getArtistAnalyticsById(artistId)
                _analytics.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Observe analytics live (for real-time updates)
    fun observeAnalytics(artistId: String): StateFlow<ArtistAnalytics?> {
        return artistStatsRepository.getArtistAnalytics(artistId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    // Example updater functions

    fun incrementPlayCount(artistId: String) = viewModelScope.launch {
        artistStatsRepository.incrementPlayCount(artistId)
    }

    fun incrementPlaylistAdds(artistId: String) = viewModelScope.launch {
        artistStatsRepository.incrementPlaylistAdds(artistId)
    }

    fun incrementWatchlistSaves(artistId: String) = viewModelScope.launch {
        artistStatsRepository.incrementWatchlistSaves(artistId)
    }

    fun updateMonthlyListeners(artistId: String, count: Int) = viewModelScope.launch {
        artistStatsRepository.updateMonthlyListeners(artistId, count)
    }

    fun updateTopSongs(artistId: String, songName: String) = viewModelScope.launch {
        val current = artistStatsRepository.getArtistAnalyticsById(artistId) ?: return@launch
        val updatedMap = current.topSongs.toMutableMap().apply {
            put(songName, getOrDefault(songName, 0) + 1)
        }
        val updated = current.copy(topSongs = updatedMap, lastUpdated = Date())
        artistStatsRepository.addArtistAnalytics(updated)
    }

    fun updatePlayCountByDay(artistId: String, dateString: String) = viewModelScope.launch {
        val current = artistStatsRepository.getArtistAnalyticsById(artistId) ?: return@launch
        val updatedMap = current.playCountByDay.toMutableMap().apply {
            put(dateString, getOrDefault(dateString, 0) + 1)
        }
        val updated = current.copy(playCountByDay = updatedMap, lastUpdated = Date())
        artistStatsRepository.addArtistAnalytics(updated)
    }

    fun updateFollowerCount(artistId: String, increment: Boolean = true) = viewModelScope.launch {
        val current = artistStatsRepository.getArtistAnalyticsById(artistId) ?: return@launch
        val updated = current.copy(
            newFollowers = current.newFollowers + if (increment) 1 else -1,
            lastUpdated = Date()
        )
        artistStatsRepository.addArtistAnalytics(updated)
    }
}
