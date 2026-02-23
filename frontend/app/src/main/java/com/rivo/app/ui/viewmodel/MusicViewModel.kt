package com.rivo.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rivo.app.data.model.Music
import com.rivo.app.data.repository.ArtistStatsRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.utils.SimpleMediaAccessHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val artistStatsRepository: ArtistStatsRepository,
    private val exoPlayer: ExoPlayer,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _allMusic = MutableStateFlow<List<Music>>(emptyList())
    val allMusic: StateFlow<List<Music>> = _allMusic.asStateFlow()

    private val _favoriteMusic = MutableStateFlow<List<Music>>(emptyList())
    val favoriteMusic: StateFlow<List<Music>> = _favoriteMusic.asStateFlow()

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> = _currentMusic.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _trendingMusic = MutableStateFlow<List<Music>>(emptyList())
    val trendingMusic: StateFlow<List<Music>> = _trendingMusic.asStateFlow()

    private val _newReleases = MutableStateFlow<List<Music>>(emptyList())
    val newReleases: StateFlow<List<Music>> = _newReleases.asStateFlow()

    private val _repeatMode = MutableStateFlow(0) // 0: off, 1: repeat one, 2: repeat all
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Add error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Add loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Add debug state to track music path
    private val _debugInfo = MutableStateFlow<String?>(null)
    val debugInfo: StateFlow<String?> = _debugInfo.asStateFlow()

    // Tracks whether we've already recorded a qualifying play for the current track
    private var playCountJob: Job? = null

    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration
                    Log.d("MusicViewModel", "Player STATE_READY, duration: ${exoPlayer.duration}")
                }
                Player.STATE_ENDED -> {
                    Log.d("MusicViewModel", "Player STATE_ENDED")
                    skipToNext()
                }
                Player.STATE_BUFFERING -> {
                    Log.d("MusicViewModel", "Player STATE_BUFFERING")
                }
                Player.STATE_IDLE -> {
                    Log.d("MusicViewModel", "Player STATE_IDLE")
                }
                else -> {
                    Log.d("MusicViewModel", "Player state: $state")
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d("MusicViewModel", "onIsPlayingChanged: $isPlaying")
            _isPlaying.value = isPlaying
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Log.e("MusicViewModel", "Player error: ${error.message}", error)
            _error.value = "Playback error: ${error.message}"
            _isLoading.value = false

            // Try to recover by reloading the current music
            _currentMusic.value?.let { music ->
                viewModelScope.launch {
                    delay(1000) // Wait a bit before retrying
                    tryAlternativePlayback(music)
                }
            }
        }
    }

    init {
        exoPlayer.addListener(playbackStateListener)
        loadAllMusic()
        loadFavorites()
        loadTrendingMusic()
        loadNewReleases()

        // Clear old audio cache files on startup
        SimpleMediaAccessHelper.clearAudioCache(context)
    }

    private fun loadAllMusic() {
        viewModelScope.launch {
            try {
                musicRepository.getAllMusic().collectLatest { _allMusic.value = it }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading all music: ${e.message}")
                _error.value = "Failed to load music: ${e.message}"
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                musicRepository.getFavoriteMusic().collectLatest { favorites ->
                    _favoriteMusic.value = favorites
                    _currentMusic.value?.let { current ->
                        _isFavorite.value = favorites.any { it.id == current.id }
                    }
                    updateFavoriteStatusInAllLists(favorites)
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading favorites: ${e.message}")
                _error.value = "Failed to load favorites: ${e.message}"
            }
        }
    }

    private fun updateFavoriteStatusInAllLists(favorites: List<Music>) {
        val favoriteIds = favorites.map { it.id }.toSet()

        // Update favorite status in all music
        _allMusic.value = _allMusic.value.map { music ->
            if (music.id in favoriteIds) {
                music.copy(isFavorite = true)
            } else {
                music.copy(isFavorite = false)
            }
        }

        // Update trending music
        _trendingMusic.value = _trendingMusic.value.map { music ->
            if (music.id in favoriteIds) {
                music.copy(isFavorite = true)
            } else {
                music.copy(isFavorite = false)
            }
        }

        // Update new releases
        _newReleases.value = _newReleases.value.map { music ->
            if (music.id in favoriteIds) {
                music.copy(isFavorite = true)
            } else {
                music.copy(isFavorite = false)
            }
        }
    }

    private fun loadTrendingMusic() {
        viewModelScope.launch {
            try {
                musicRepository.getTrendingMusic().collectLatest { _trendingMusic.value = it }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading trending music: ${e.message}")
                _error.value = "Failed to load trending music: ${e.message}"
            }
        }
    }

    private fun loadNewReleases() {
        viewModelScope.launch {
            try {
                musicRepository.getNewReleases().collectLatest { _newReleases.value = it }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading new releases: ${e.message}")
                _error.value = "Failed to load new releases: ${e.message}"
            }
        }
    }

    fun getMusicById(musicId: String) {
        viewModelScope.launch {
            try {
                val music = musicRepository.getMusicById(musicId)
                _currentMusic.value = music
                _isFavorite.value = music?.isFavorite ?: false
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error getting music by ID: ${e.message}")
                _error.value = "Failed to get music: ${e.message}"
            }
        }
    }

    fun loadMusic(musicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("MusicViewModel", "Loading music with ID: $musicId")
                val music = musicRepository.getMusicById(musicId)
                if (music != null) {
                    _currentMusic.value = music

                    // Debug info
                    Log.d("MusicViewModel", "Music found: ${music.title}, path: ${music.path}")
                    _debugInfo.value = "Path: ${music.path?.take(50)}..."

                    playMusic(music)
                } else {
                    Log.e("MusicViewModel", "Music not found with ID: $musicId")
                    _error.value = "Music not found"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading music: ${e.message}")
                _error.value = "Failed to load music: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun playMusic(music: Music) {
        try {
            Log.d("MusicViewModel", "Playing music: ${music.title} with path: ${music.path}")
            _currentMusic.value = music
            _isFavorite.value = music.isFavorite
            _isLoading.value = true

            if (music.path.isNullOrEmpty()) {
                Log.e("MusicViewModel", "Cannot play music: path is null or empty")
                _error.value = "Cannot play music: file not found"
                _isLoading.value = false
                return
            }

            exoPlayer.stop()
            exoPlayer.clearMediaItems()

            val playableUri = SimpleMediaAccessHelper.getPlayableUri(context, music.path)

            if (playableUri == null) {
                Log.e("MusicViewModel", "Failed to get playable URI for: ${music.path}")
                _error.value = "Cannot play music: permission denied or file missing"
                _isLoading.value = false
                return
            }

            _debugInfo.value = "Original: ${music.path?.take(30)}...\nResolved: $playableUri"
            Log.d("MusicViewModel", "Using playable URI: $playableUri")

            val mediaItem = MediaItem.Builder()
                .setUri(playableUri)
                .setMediaId(music.id)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            viewModelScope.launch {
                delay(800)
                exoPlayer.play()
                _isLoading.value = false
            }

            // Start a job that will record a play only after at least 45 seconds
            // of listening for this specific track (and only once per user).
            playCountJob?.cancel()
            playCountJob = viewModelScope.launch {
                val trackId = music.id
                val thresholdMs = 45_000L

                try {
                    while (true) {
                        delay(1000)

                        // Stop if user changed tracks or playback stopped
                        val current = _currentMusic.value ?: break
                        if (current.id != trackId || !_isPlaying.value) {
                            continue
                        }

                        val position = exoPlayer.currentPosition
                        if (position >= thresholdMs) {
                            try {
                                musicRepository.incrementPlayCountIfFirstTime(trackId)
                                artistStatsRepository.incrementArtistPlayCount(current.artistId ?: "")
                            } catch (e: Exception) {
                                Log.e("MusicViewModel", "Error updating play stats after threshold: ${e.message}")
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MusicViewModel", "Play count job error: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.e("MusicViewModel", "Error playing music: ${e.message}", e)
            if (e is SecurityException && e.message?.contains("Permission Denial") == true) {
                _error.value = "Permission denied: Please select the file again"
                // Try alternative playback as a fallback
                tryAlternativePlayback(music)
            } else {
                _error.value = "Failed to play music: ${e.message}"
            }
            _isLoading.value = false
        }
    }


    private fun tryAlternativePlayback(music: Music) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val originalUri = when {
                    music.path?.startsWith("content://") == true -> music.path.toUri()
                    music.path?.startsWith("/") == true -> File(music.path).toUri()
                    music.path?.startsWith("http") == true -> music.path.toUri()
                    else -> "file://${music.path}".toUri()
                }

                val cacheDir = File(context.cacheDir, "music_cache")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.mp3")

                try {
                    context.contentResolver.openInputStream(originalUri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val localUri = Uri.fromFile(tempFile)

                    Log.d("MusicViewModel", "Playing from local copy: $localUri")
                    _debugInfo.value = "Using local copy: $localUri"

                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()

                    val mediaItem = MediaItem.Builder()
                        .setUri(localUri)
                        .setMediaId(music.id)
                        .build()

                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()

                    delay(800)
                    exoPlayer.play()
                    _isLoading.value = false
                } catch (e: Exception) {
                    Log.e("MusicViewModel", "Failed to create local copy: ${e.message}")
                    _error.value = "Cannot play music: ${e.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Alternative playback failed: ${e.message}")
                _error.value = "Cannot play music: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun pauseMusic() {
        exoPlayer.pause()
    }
    fun stopMusic() {
        exoPlayer.stop()
        _isPlaying.value = false
        _currentMusic.value = null // clear current song
    }


    fun resumeMusic() {
        if (_currentMusic.value != null) {
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                _currentMusic.value?.let { playMusic(it) }
            } else {
                exoPlayer.play()
            }
        } else {
            _allMusic.value.firstOrNull()?.let { playMusic(it) }
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun skipToNext() {
        val current = _currentMusic.value ?: return
        val list = _allMusic.value
        if (list.isEmpty()) return

        val next = if (_isShuffleEnabled.value) {
            list.filter { it.id != current.id }.randomOrNull()
        } else {
            val idx = list.indexOfFirst { it.id == current.id }
            when {
                idx == -1 -> null
                idx == list.size - 1 && _repeatMode.value == 2 -> list.firstOrNull()
                idx < list.size - 1 -> list[idx + 1]
                else -> null
            }
        }

        next?.let { playMusic(it) }
    }

    fun skipToPrevious() {
        val current = _currentMusic.value ?: return
        val list = _allMusic.value
        if (list.isEmpty()) return

        if (exoPlayer.currentPosition > 3000) {
            exoPlayer.seekTo(0)
            return
        }

        val previous = if (_isShuffleEnabled.value) {
            list.filter { it.id != current.id }.randomOrNull()
        } else {
            val idx = list.indexOfFirst { it.id == current.id }
            when {
                idx == -1 -> null
                idx == 0 && _repeatMode.value == 2 -> list.lastOrNull()
                idx > 0 -> list[idx - 1]
                else -> null
            }
        }

        previous?.let { playMusic(it) }
    }

    fun toggleRepeatMode() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
        exoPlayer.repeatMode = when (_repeatMode.value) {
            0 -> Player.REPEAT_MODE_OFF
            1 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_ALL
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        exoPlayer.shuffleModeEnabled = _isShuffleEnabled.value
    }

    fun toggleFavorite(musicId: String) {
        viewModelScope.launch {
            try {
                val music = _allMusic.value.find { it.id == musicId } ?: return@launch
                val newState = !music.isFavorite

                musicRepository.toggleFavorite(musicId, newState)

                if (_currentMusic.value?.id == musicId) {
                    _currentMusic.value = _currentMusic.value!!.copy(isFavorite = newState)
                    _isFavorite.value = newState
                }

                loadFavorites()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error toggling favorite: ${e.message}")
                _error.value = "Failed to update favorite status: ${e.message}"
            }
        }
    }

    fun updateCurrentPosition() {
        if (exoPlayer.isPlaying) {
            _currentPosition.value = exoPlayer.currentPosition
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearDebugInfo() {
        _debugInfo.value = null
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(playbackStateListener)
        exoPlayer.release()
    }
}
