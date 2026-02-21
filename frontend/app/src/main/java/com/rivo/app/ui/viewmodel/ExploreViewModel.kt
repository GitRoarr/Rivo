package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.BannerItem
import com.rivo.app.data.remote.MusicCategory
import com.rivo.app.data.repository.FeaturedContentRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val featuredContentRepository: FeaturedContentRepository,
    private val musicRepository: MusicRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _featuredBanner = MutableStateFlow<FeaturedContent?>(null)
    val featuredBanner: StateFlow<FeaturedContent?> = _featuredBanner
    
    private val _featuredMusic = MutableStateFlow<Music?>(null)
    val featuredMusic: StateFlow<Music?> = _featuredMusic

    // For the horizontal artist list
    private val _artists = MutableStateFlow<List<User>>(emptyList())
    val artists: StateFlow<List<User>> = _artists
    
    // Alias for HomeScreen compatibility
    val featuredArtists: StateFlow<List<User>> = _artists

    // For the song grid / generic songs
    private val _songs = MutableStateFlow<List<Music>>(emptyList())
    val songs: StateFlow<List<Music>> = _songs

    private val _trendingMusic = MutableStateFlow<List<Music>>(emptyList())
    val trendingMusic: StateFlow<List<Music>> = _trendingMusic

    private val _newReleases = MutableStateFlow<List<Music>>(emptyList())
    val newReleases: StateFlow<List<Music>> = _newReleases
    
    private val _categories = MutableStateFlow<List<MusicCategory>>(emptyList())
    val categories: StateFlow<List<MusicCategory>> = _categories

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        refresh()
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = musicRepository.getExploreData()
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    _trendingMusic.value = it.trendingMusic
                    _newReleases.value = it.newReleases
                    _artists.value = it.featuredArtists
                    _songs.value = it.featuredMusic
                    _categories.value = it.categories ?: emptyList()
                    
                    // Handle banner
                    if (it.banners.isNotEmpty()) {
                        val firstBanner = it.banners[0]
                        _featuredBanner.value = FeaturedContent(
                            id = firstBanner.id,
                            contentId = "", // action url might contain id
                            type = FeaturedType.BANNER,
                            title = firstBanner.title,
                            description = firstBanner.subtitle,
                            imageUrl = firstBanner.imageUrl,
                            createdBy = "admin",
                            featuredBy = "admin"
                        )
                    }
                }
            }
            _isLoading.value = false
        }
    }

    private fun loadFeaturedBanner() {
        viewModelScope.launch {
            try {
                val banner = featuredContentRepository.getLatestFeaturedBanner(FeaturedType.BANNER)
                _featuredBanner.value = banner

                val musicId = banner?.contentId
                if (!musicId.isNullOrEmpty()) {
                    val music = musicRepository.getMusicById(musicId)
                    _featuredMusic.value = music
                } else {
                    _featuredMusic.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _featuredBanner.value = null
                _featuredMusic.value = null
            }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            // Get all artists
            userRepository.getArtists().collectLatest { artistList ->
                _artists.value = artistList
            }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
             // Use all music for the grid
            musicRepository.getAllMusic().collectLatest { musicList ->
                _songs.value = musicList.shuffled() // Shuffle for discovery feel
                _isLoading.value = false
            }
        }
    }

    private fun loadTrendingMusic() {
        viewModelScope.launch {
            musicRepository.getTrendingMusic().collectLatest {
                _trendingMusic.value = it
            }
        }
    }

    private fun loadNewReleases() {
        viewModelScope.launch {
            musicRepository.getNewReleases().collectLatest {
                _newReleases.value = it
            }
        }
    }
}