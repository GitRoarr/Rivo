package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.AnalyticsPeriod
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _artistAnalytics = MutableStateFlow<ArtistAnalytics?>(null)
    val artistAnalytics: StateFlow<ArtistAnalytics?> = _artistAnalytics.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.LAST_30_DAYS)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _topSongs = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val topSongs: StateFlow<List<Pair<String, Int>>> = _topSongs.asStateFlow()

    private val _listenerDemographics = MutableStateFlow<Map<String, Float>>(emptyMap())
    val listenerDemographics: StateFlow<Map<String, Float>> = _listenerDemographics.asStateFlow()

    private val _playCountByDay = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playCountByDay: StateFlow<Map<String, Int>> = _playCountByDay.asStateFlow()

    fun loadArtistAnalytics(artistId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val analytics = analyticsRepository.getArtistAnalytics(
                    artistId = artistId,
                    period = _selectedPeriod.value
                )
                _artistAnalytics.value = analytics

                // Load additional analytics data
                _topSongs.value = analyticsRepository.getTopSongs(artistId, _selectedPeriod.value)
                _listenerDemographics.value = analyticsRepository.getListenerDemographics(artistId)
                _playCountByDay.value = analyticsRepository.getPlayCountByDay(
                    artistId = artistId,
                    period = _selectedPeriod.value
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load analytics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setPeriod(period: AnalyticsPeriod) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            _artistAnalytics.value?.artistId?.let { artistId ->
                loadArtistAnalytics(artistId)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
