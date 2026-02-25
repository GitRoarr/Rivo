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

    // Playback queue - the actual list of songs being played through
    private val _playQueue = MutableStateFlow<List<Music>>(emptyList())
    val playQueue: StateFlow<List<Music>> = _playQueue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    // Add error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Add loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Add debug state to track music path
    private val _debugInfo = MutableStateFlow<String?>(null)
    val debugInfo: StateFlow<String?> = _debugInfo.asStateFlow()

    // ─── Play Count Tracking (YouTube-like: count after 45 seconds of real listening) ───
    companion object {
        const val PLAY_COUNT_THRESHOLD_MS = 45_000L // 45 seconds
    }
    
    // Set of music IDs that have been counted in this session (to avoid double counting)
    private val countedPlaysThisSession = mutableSetOf<String>()
    
    // Accumulated real listening time (not just position - tracks actual play time)
    private var accumulatedListenTimeMs = 0L
    private var lastCheckTimeMs = 0L
    private var lastKnownPosition = 0L
    
    // Job that monitors playback and counts plays after threshold
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
                // Ensure we actually fetch music from remote
                musicRepository.refreshMusic()
                musicRepository.getAllMusic().collectLatest { _allMusic.value = it }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading all music: ${e.message}")
                _error.value = "Failed to load music: ${e.message}"
            }
        }
    }

    fun refreshMusic() {
        viewModelScope.launch {
            try {
                musicRepository.refreshMusic()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Refresh failed: ${e.message}")
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
                
                // Ensure allMusic is loaded before building queue
                if (_allMusic.value.isEmpty()) {
                    try {
                        musicRepository.refreshMusic()
                    } catch (e: Exception) {
                        Log.e("MusicViewModel", "Failed to refresh music list: ${e.message}")
                    }
                }
                
                val music = musicRepository.getMusicById(musicId)
                if (music != null) {
                    _currentMusic.value = music

                    // Build queue from allMusic if not already set
                    if (_playQueue.value.isEmpty() && _allMusic.value.isNotEmpty()) {
                        val idx = _allMusic.value.indexOfFirst { it.id == musicId }
                        _playQueue.value = _allMusic.value
                        _queueIndex.value = if (idx >= 0) idx else 0
                        Log.d("MusicViewModel", "Queue built from allMusic: ${_allMusic.value.size} tracks, starting at index ${_queueIndex.value}")
                    } else if (_playQueue.value.isEmpty() && _allMusic.value.isEmpty()) {
                        // Last resort: create a single-item queue
                        _playQueue.value = listOf(music)
                        _queueIndex.value = 0
                        Log.d("MusicViewModel", "Queue set to single track: ${music.title}")
                    } else if (_playQueue.value.isNotEmpty()) {
                        // Update queue index to reflect current track
                        val idx = _playQueue.value.indexOfFirst { it.id == musicId }
                        if (idx >= 0) _queueIndex.value = idx
                    }

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
            _currentMusic.value = music
            _isFavorite.value = music.isFavorite
            _isLoading.value = true

            if (music.path.isNullOrEmpty()) {
                Log.e("MusicViewModel", "Cannot play music: path is null or empty")
                _error.value = "Cannot play music: file not found"
                _isLoading.value = false
                return
            }

            // Safely stop and clear — catch dead thread errors
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            } catch (e: IllegalStateException) {
                Log.w("MusicViewModel", "ExoPlayer in bad state during stop/clear: ${e.message}")
                // Player thread may be dead — still try to set media and prepare
            }

            val playableUri = SimpleMediaAccessHelper.getPlayableUri(context, music.path)

            if (playableUri == null) {
                Log.e("MusicViewModel", "Failed to get playable URI for: ${music.path}")
                _error.value = "Cannot play music: permission denied or file missing"
                _isLoading.value = false
                return
            }

            val mediaItem = MediaItem.Builder()
                .setUri(playableUri)
                .setMediaId(music.id)
                .build()

            try {
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } catch (e: IllegalStateException) {
                Log.e("MusicViewModel", "ExoPlayer thread is dead, cannot play: ${e.message}")
                _error.value = "Player error — please restart the app"
                _isLoading.value = false
                return
            }

            viewModelScope.launch {
                delay(800)
                try {
                    exoPlayer.play()
                } catch (e: IllegalStateException) {
                    Log.e("MusicViewModel", "ExoPlayer cannot play: ${e.message}")
                    _error.value = "Player error — please restart the app"
                }
                _isLoading.value = false
            }

            // ─── YouTube-like Play Count Logic ───────────────────────────────────────
            // Count a play only after 45 seconds of ACTUAL listening (not seeking).
            // Only count once per track per session to prevent gaming.
            playCountJob?.cancel()
            
            // Reset tracking for new track
            accumulatedListenTimeMs = 0L
            lastCheckTimeMs = System.currentTimeMillis()
            lastKnownPosition = 0L
            
            playCountJob = viewModelScope.launch {
                val trackId = music.id
                
                // Skip if already counted this track in this session
                if (countedPlaysThisSession.contains(trackId)) {
                    Log.d("MusicViewModel", "Play already counted for $trackId this session")
                    return@launch
                }

                try {
                    while (true) {
                        delay(1000) // Check every second
                        
                        val current = _currentMusic.value ?: break
                        if (current.id != trackId) break // Track changed
                        
                        if (_isPlaying.value) {
                            val currentPosition = exoPlayer.currentPosition
                            val currentTime = System.currentTimeMillis()
                            val timeSinceLastCheck = currentTime - lastCheckTimeMs
                            
                            // Calculate position delta - if user seeked, the delta will be very different from time passed
                            val positionDelta = currentPosition - lastKnownPosition
                            
                            // Only count as real listening if position moved forward normally (not seeked)
                            // Allow some tolerance for buffering/timing issues (±500ms)
                            val expectedDelta = timeSinceLastCheck
                            val isNormalPlayback = positionDelta >= 0 && 
                                                   positionDelta <= expectedDelta + 500 &&
                                                   positionDelta >= expectedDelta - 500
                            
                            if (isNormalPlayback && positionDelta > 0) {
                                accumulatedListenTimeMs += positionDelta
                            }
                            
                            lastCheckTimeMs = currentTime
                            lastKnownPosition = currentPosition
                            
                            // Check if threshold reached
                            if (accumulatedListenTimeMs >= PLAY_COUNT_THRESHOLD_MS) {
                                Log.d("MusicViewModel", "Play threshold reached for $trackId after ${accumulatedListenTimeMs}ms of listening")
                                
                                // Mark as counted immediately to prevent race conditions
                                countedPlaysThisSession.add(trackId)
                                
                                try {
                                    musicRepository.incrementPlayCountIfFirstTime(trackId)
                                    current.artistId?.let { artistId ->
                                        artistStatsRepository.incrementArtistPlayCount(artistId)
                                    }
                                    Log.d("MusicViewModel", "Play count incremented for $trackId")
                                } catch (e: Exception) {
                                    Log.e("MusicViewModel", "Error incrementing play count: ${e.message}")
                                    // Remove from counted set so it can retry
                                    countedPlaysThisSession.remove(trackId)
                                }
                                break
                            }
                        } else {
                            // Update timing when paused so we don't count pause time
                            lastCheckTimeMs = System.currentTimeMillis()
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
        val queue = _playQueue.value
        if (queue.isEmpty()) {
            // Fallback: try allMusic
            val allList = _allMusic.value
            if (allList.isNotEmpty()) {
                setQueue(allList)
                return skipToNext()
            }
            Log.d("MusicViewModel", "skipToNext: no queue available")
            return
        }

        val currentIdx = _queueIndex.value

        val nextIdx = if (_isShuffleEnabled.value) {
            val available = queue.indices.filter { it != currentIdx }
            if (available.isEmpty()) return
            available.random()
        } else {
            when {
                currentIdx < queue.size - 1 -> currentIdx + 1
                _repeatMode.value == 2 -> 0 // wrap around
                else -> return // end of queue, no repeat
            }
        }

        _queueIndex.value = nextIdx
        playMusic(queue[nextIdx])
    }

    fun skipToPrevious() {
        // If more than 3 seconds in, restart current track
        if (exoPlayer.currentPosition > 3000) {
            exoPlayer.seekTo(0)
            return
        }

        val queue = _playQueue.value
        if (queue.isEmpty()) {
            val allList = _allMusic.value
            if (allList.isNotEmpty()) {
                setQueue(allList)
                return skipToPrevious()
            }
            Log.d("MusicViewModel", "skipToPrevious: no queue available")
            return
        }

        val currentIdx = _queueIndex.value

        val prevIdx = if (_isShuffleEnabled.value) {
            val available = queue.indices.filter { it != currentIdx }
            if (available.isEmpty()) return
            available.random()
        } else {
            when {
                currentIdx > 0 -> currentIdx - 1
                _repeatMode.value == 2 -> queue.size - 1 // wrap around
                else -> return // beginning of queue, no repeat
            }
        }

        _queueIndex.value = prevIdx
        playMusic(queue[prevIdx])
    }

    /** Set a playback queue and start from a specific track */
    fun setQueue(queue: List<Music>, startIndex: Int = -1) {
        _playQueue.value = queue
        if (startIndex >= 0 && startIndex < queue.size) {
            _queueIndex.value = startIndex
        }
    }

    /** Set queue and play a specific track from it */
    fun playFromQueue(queue: List<Music>, music: Music) {
        val idx = queue.indexOfFirst { it.id == music.id }
        _playQueue.value = queue
        _queueIndex.value = if (idx >= 0) idx else 0
        playMusic(music)
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
                // Use _isFavorite for the current playing track (most up-to-date state);
                // fall back to _allMusic for other tracks.
                val currentIsFavorite = if (_currentMusic.value?.id == musicId) {
                    _isFavorite.value
                } else {
                    _allMusic.value.find { it.id == musicId }?.isFavorite ?: false
                }
                val newState = !currentIsFavorite

                // Optimistically update UI state first
                if (_currentMusic.value?.id == musicId) {
                    _currentMusic.value = _currentMusic.value!!.copy(isFavorite = newState)
                    _isFavorite.value = newState
                }

                musicRepository.toggleFavorite(musicId, newState)
                loadFavorites()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error toggling favorite: ${e.message}")
                _error.value = "Failed to update favorite status: ${e.message}"
                // Revert optimistic UI update on failure
                if (_currentMusic.value?.id == musicId) {
                    _isFavorite.value = !_isFavorite.value
                    _currentMusic.value = _currentMusic.value?.copy(isFavorite = _isFavorite.value)
                }
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
        playCountJob?.cancel()
        exoPlayer.removeListener(playbackStateListener)
        // Do NOT call exoPlayer.release() here!
        // The ExoPlayer is a @Singleton provided by Hilt DI — releasing it
        // kills its internal thread permanently. When a new MusicViewModel is
        // created, it receives the same dead instance, causing
        // "Handler on a dead thread" errors and playback failure.
        // Just stop playback; the DI container manages the ExoPlayer lifecycle.
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }
}
