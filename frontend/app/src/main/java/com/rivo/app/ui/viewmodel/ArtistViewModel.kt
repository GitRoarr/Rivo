package com.rivo.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.repository.ArtistStatsRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.data.repository.UserRepository
import com.rivo.app.utils.ImagePickerHelper
import com.rivo.app.utils.MediaAccessHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val musicRepository: MusicRepository,
    private val artistStatsRepository: ArtistStatsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _artists = MutableStateFlow<List<User>>(emptyList())
    val artists: StateFlow<List<User>> = _artists.asStateFlow()

    private val _currentArtist = MutableStateFlow<User?>(null)
    val currentArtist: StateFlow<User?> = _currentArtist.asStateFlow()

    private val _artistMusic = MutableStateFlow<List<Music>>(emptyList())
    val artistMusic: StateFlow<List<Music>> = _artistMusic.asStateFlow()

    private val _artistAnalytics = MutableStateFlow<ArtistAnalytics?>(null)
    val artistAnalytics: StateFlow<ArtistAnalytics?> = _artistAnalytics.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus.asStateFlow()

    private val _selectedTab = MutableStateFlow(ArtistDashboardTab.UPLOAD_MUSIC)
    val selectedTab: StateFlow<ArtistDashboardTab> = _selectedTab.asStateFlow()

    private val _categories = MutableStateFlow<List<com.rivo.app.data.remote.MusicCategory>>(emptyList())
    val categories: StateFlow<List<com.rivo.app.data.remote.MusicCategory>> = _categories.asStateFlow()

    init {
        loadArtists()
        loadCategories()

        viewModelScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (session.userType == UserType.ARTIST) {
                val artist = userRepository.getUserByEmail(session.email)
                artist?.let {
                    _currentArtist.value = it
                    loadArtistData(it.id)
                }
            }
        }
    }

    fun setSelectedTab(tab: ArtistDashboardTab) {
        _selectedTab.value = tab
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = musicRepository.getCategories()
            result.onSuccess {
                _categories.value = it
            }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            userRepository.getArtists().collectLatest { artistList ->
                _artists.value = artistList
            }
        }
    }

    fun loadArtistData(artistId: String) {
        viewModelScope.launch {
            // Load artist profile
            val artist = userRepository.getUserById(artistId)
            if (artist != null) {
                _currentArtist.value = artist
            }
        }

        viewModelScope.launch {
            // Load artist music directly from backend (MongoDB Atlas)
            val result = musicRepository.refreshArtistMusic(artistId)
            result.onSuccess { musicList ->
                _artistMusic.value = musicList
                Log.d("ArtistViewModel", "Loaded ${musicList.size} music tracks for artist $artistId from Atlas")
            }.onFailure { e ->
                Log.e("ArtistViewModel", "Error loading artist music from backend", e)
                _artistMusic.value = emptyList()
            }
        }

        viewModelScope.launch {
            try {
                // Initial load from Local DB
                artistStatsRepository.getArtistAnalytics(artistId).collectLatest { analytics ->
                    if (analytics != null) {
                        _artistAnalytics.value = analytics
                    }
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Error loading local analytics", e)
            }
        }

        viewModelScope.launch {
            // Refresh from Backend
            artistStatsRepository.refreshArtistStats().onSuccess { analytics ->
                // Update with correct artist ID before saving
                val analyticsWithId = analytics.copy(artistId = artistId)
                artistStatsRepository.addArtistAnalytics(analyticsWithId)
            }.onFailure { e ->
                Log.e("ArtistViewModel", "Error refreshing backend stats", e)
            }
        }
    }

    fun updateArtistApprovalStatus(artistId: String, approved: Boolean) {
        viewModelScope.launch {
            userRepository.approveArtist(artistId, approved)
            loadArtists()
        }
    }




    fun uploadMusic(
        context: Context,
        title: String,
        genre: String?,
        description: String?,
        audioUri: Uri?,
        coverImageUri: Uri?
    ) {
        if (audioUri == null) {
            _operationStatus.value = "No audio file selected"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0f

            try {
                val session = sessionManager.sessionFlow.first()
                val artist = userRepository.getUserByEmail(session.email) ?: throw Exception("Artist not found")
                val userId = sessionManager.getCurrentUserId()

                Log.d("MusicUpload", "Starting upload for artist: ${artist.id}, title: $title")

                val resolvedAudioUri = MediaAccessHelper.resolveUri(context, audioUri)
                val resolvedCoverUri = coverImageUri?.let { MediaAccessHelper.resolveUri(context, it) }


                if (!MediaAccessHelper.isUriAccessible(context, resolvedAudioUri)) {
                    throw Exception("Cannot access audio file. Please select a different file.")
                }

                resolvedCoverUri?.let {
                    if (!MediaAccessHelper.isUriAccessible(context, it)) {
                        throw Exception("Cannot access cover image. Please select a different image.")
                    }
                }

                val coverImagePath = resolvedCoverUri?.let { uri ->
                    ImagePickerHelper.saveImageToInternalStorage(
                        context,
                        uri,
                        "temp_cover_${System.currentTimeMillis()}.jpg"
                    )
                }

                val audioDuration = musicRepository.getAudioDuration(context, resolvedAudioUri)
                
                // Call real upload to Cloudinary/Backend
                val result = musicRepository.uploadMusic(
                    title = title,
                    genre = genre ?: "Unknown",
                    album = title,
                    duration = audioDuration,
                    audioPath = resolvedAudioUri.toString(),
                    coverImagePath = coverImagePath // This is the local cover image if saved
                )

                if (result.isSuccess) {
                    val uploadedMusic = result.getOrNull()
                    Log.d("MusicUpload", "Music uploaded successfully: ${uploadedMusic?.id}")
                    _operationStatus.value = "Music uploaded successfully"
                    
                    // Switch to My Music tab after successful upload
                    _selectedTab.value = ArtistDashboardTab.MY_MUSIC
                    loadArtistData(artist.id)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("MusicUpload", "Upload failed: $error")
                    _operationStatus.value = "Upload failed: $error"
                }

                _uploadProgress.value = null
                _isUploading.value = false

            } catch (e: Exception) {
                Log.e("MusicUpload", "Error during music upload: ${e.message}", e)
                _uploadProgress.value = null
                _isUploading.value = false
                _operationStatus.value = "Upload failed: ${e.message}"
            }
        }
    }


    fun deleteMusic(musicId: String) {
        viewModelScope.launch {
            try {
                musicRepository.deleteMusic(musicId)
                _operationStatus.value = "Music deleted successfully"

                val session = sessionManager.sessionFlow.first()
                val artist = userRepository.getUserByEmail(session.email) ?: return@launch
                loadArtistData(artist.id)
            } catch (e: Exception) {
                _operationStatus.value = "Failed to delete music: ${e.message}"
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }
}

enum class ArtistDashboardTab {
    UPLOAD_MUSIC, MY_MUSIC, ANALYTICS
}
