package com.rivo.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.content.ContextCompat
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    private val notificationRepository: NotificationRepository,
    private val artistStatsRepository: ArtistStatsRepository
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> = _currentMusic.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _queue = MutableStateFlow<List<Music>>(emptyList())
    val queue: StateFlow<List<Music>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    // Play count tracking - YouTube-like logic (count after 45 seconds of listening)
    companion object {
        const val PLAY_COUNT_THRESHOLD_MS = 45_000L // 45 seconds in milliseconds
    }
    
    // Set of music IDs that have been counted in this session (to avoid double counting)
    private val countedPlays = mutableSetOf<String>()
    
    // Accumulated listening time for current track (persists across pause/resume)
    private var accumulatedListenTimeMs = 0L
    private var lastPlayStartTime = 0L

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setOnCompletionListener {
                when (_repeatMode.value) {
                    RepeatMode.ONE -> {
                        seekTo(0)
                        start()
                    }
                    RepeatMode.ALL -> {
                        if (_queueIndex.value < _queue.value.size - 1) {
                            playNext()
                        } else {
                            _queueIndex.value = 0
                            playQueueItem(_queueIndex.value)
                        }
                    }
                    RepeatMode.NONE -> {
                        if (_queueIndex.value < _queue.value.size - 1) {
                            playNext()
                        } else {
                            _isPlaying.value = false
                        }
                    }
                }
            }

            setOnPreparedListener { mp ->
                try {
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    
                    // Start tracking listen time for play count
                    lastPlayStartTime = System.currentTimeMillis()

                    // Update notification
                    _currentMusic.value?.let { music ->
                        notificationRepository.showNowPlayingNotification(music, true)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    // Handle permission issue if needed (e.g., show message to user)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    // Handle if mediaPlayer is not in a correct state
                }
            }


            setOnErrorListener { _, _, _ ->
                _isPlaying.value = false
                true
            }
        }
    }

    fun playMusic(music: Music) {
        try {
            _currentMusic.value = music
            
            // Reset listening time tracking for new song
            accumulatedListenTimeMs = 0L
            lastPlayStartTime = 0L

            mediaPlayer?.reset()

            val uri = if (music.path?.startsWith("http") == true) {
                Uri.parse(music.path)
            } else {
                Uri.parse("file://${music.path}")
            }

            if (uri.scheme == "file") {
                val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    throw SecurityException("READ_EXTERNAL_STORAGE permission not granted")
                }
            }

            mediaPlayer?.apply {
                setDataSource(context, uri)
                prepareAsync()
            }

            if (_queue.value.none { it.id == music.id }) {
                _queue.value = _queue.value + music
                _queueIndex.value = _queue.value.size - 1
            } else {
                val index = _queue.value.indexOfFirst { it.id == music.id }
                if (index != -1) {
                    _queueIndex.value = index
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Optional: notify user that permission is missing
        }
    }



    fun play() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true

            _currentMusic.value?.let { music ->
                notificationRepository.showNowPlayingNotification(music, true)
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun playNext() {
        if (_queue.value.isEmpty()) return

        val nextIndex = if (_isShuffleEnabled.value) {
            val availableIndices = _queue.value.indices.filter { it != _queueIndex.value }
            if (availableIndices.isEmpty()) return
            availableIndices.random()
        } else {
            (_queueIndex.value + 1) % _queue.value.size
        }

        _queueIndex.value = nextIndex
        playQueueItem(nextIndex)
    }

    private fun playQueueItem(index: Int) {
        if (index in _queue.value.indices) {
            playMusic(_queue.value[index])
        }
    }

    fun playPrevious() {
        if (_queue.value.isEmpty()) return
        val prevIndex = if (_queueIndex.value > 0) {
            _queueIndex.value - 1
        } else {
            _queue.value.size - 1 // wrap around
        }
        _queueIndex.value = prevIndex
        playQueueItem(prevIndex)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        notificationRepository.cancelAllNotifications()
    }
}
