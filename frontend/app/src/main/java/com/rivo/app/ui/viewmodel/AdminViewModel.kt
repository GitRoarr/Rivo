package com.rivo.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.repository.FeaturedContentRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.data.repository.UserRepository
import com.rivo.app.utils.ImagePickerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val featuredContentRepository: FeaturedContentRepository,
    private val musicRepository: MusicRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentAdmin = MutableStateFlow<User?>(null)
    val currentAdmin: StateFlow<User?> = _currentAdmin

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _featuredSongs = MutableStateFlow<List<Music>>(emptyList())
    val featuredSongs: StateFlow<List<Music>> = _featuredSongs

    private val _featuredArtists = MutableStateFlow<List<User>>(emptyList())
    val featuredArtists: StateFlow<List<User>> = _featuredArtists

    private val _allMusic = MutableStateFlow<List<Music>>(emptyList())
    val allMusic: StateFlow<List<Music>> = _allMusic

    private val _platformStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val platformStats: StateFlow<Map<String, Int>> = _platformStats

    private val _pendingVerifications = MutableStateFlow<List<User>>(emptyList())
    val pendingVerifications: StateFlow<List<User>> = _pendingVerifications

    private val _pendingMusic = MutableStateFlow<List<Music>>(emptyList())
    val pendingMusic: StateFlow<List<Music>> = _pendingMusic

    private val _featuredContent = MutableStateFlow<List<FeaturedContent>>(emptyList())
    val featuredContent: StateFlow<List<FeaturedContent>> = _featuredContent

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus

    init {
        viewModelScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (session.userType == UserType.ADMIN) {
                val admin = userRepository.getUserByEmail(session.email)
                _currentAdmin.value = admin
            }
            loadAllData()
        }
    }

    private fun loadAllData() {
        loadAllUsers()
        loadAllMusic()
        loadPlatformStats()
        loadPendingVerifications()
        loadPendingMusic()
        loadFeaturedContent()
        loadFeaturedSongs()
        loadFeaturedArtists()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collectLatest { users ->
                _allUsers.value = users
            }
        }
    }

    private fun loadAllMusic() {
        viewModelScope.launch {
            musicRepository.getAllMusic().collectLatest { musics ->
                _allMusic.value = musics
            }
        }
    }

    private fun loadPlatformStats() {
        viewModelScope.launch {
            val users = userRepository.getAllUsers().first()
            val musics = musicRepository.getAllMusic().first()

            _platformStats.value = mapOf(
                "totalUsers" to users.size,
                "totalArtists" to users.count { it.userType == UserType.ARTIST },
                "totalSongs" to musics.size,
                "totalPlays" to musics.sumOf { it.playCount }
            )
        }
    }

    private fun loadPendingVerifications() {
        viewModelScope.launch {
            userRepository.getUsersAwaitingVerification().collectLatest { pendingUsers ->
                _pendingVerifications.value = pendingUsers
            }
        }
    }

    private fun loadPendingMusic() {
        viewModelScope.launch {
            musicRepository.getPendingApprovalMusic().collectLatest { pendingMusics ->
                _pendingMusic.value = pendingMusics
            }
        }
    }

    private fun loadFeaturedContent() {
        viewModelScope.launch {
            featuredContentRepository.getAllFeaturedContent().collectLatest { contents ->
                _featuredContent.value = contents
            }
        }
    }

    fun suspendUser(userId: String) {
        viewModelScope.launch {
            userRepository.suspendUser(userId, true)
            _operationStatus.value = "User suspended"
            loadAllUsers()
        }
    }

    fun makeAdmin(userId: String) {
        viewModelScope.launch {
            userRepository.promoteUserToAdmin(userId)
            _operationStatus.value = "User promoted to Admin"
            loadAllUsers()
        }
    }

    fun approveMusic(musicId: String) {
        viewModelScope.launch {
            musicRepository.approveMusic(musicId)
            _operationStatus.value = "Music approved"
            loadPendingMusic()
            loadAllMusic()
        }
    }

    fun rejectMusic(musicId: String) {
        viewModelScope.launch {
            musicRepository.rejectMusic(musicId)
            _operationStatus.value = "Music rejected"
            loadPendingMusic()
        }
    }

    fun approveVerification(userId: String) {
        viewModelScope.launch {
            userRepository.approveUserVerification(userId)
            _operationStatus.value = "Verification approved"
            loadPendingVerifications()
        }
    }

    fun rejectVerification(userId: String) {
        viewModelScope.launch {
            userRepository.rejectUserVerification(userId)
            _operationStatus.value = "Verification rejected"
            loadPendingVerifications()
        }
    }

    fun featureMusic(music: Music) {
        viewModelScope.launch {
            _currentAdmin.value?.let { admin ->
                // Use the local file path for the artwork if available
                val artworkUri = music.artworkUri

                val featured = FeaturedContent(
                    id = UUID.randomUUID().toString(),
                    title = music.title,
                    description = null,
                    imageUrl = artworkUri,
                    type = FeaturedType.SONG,
                    contentId = music.id,
                    createdBy = admin.id,
                    featuredBy = admin.id,
                    position = 0
                )
                featuredContentRepository.insertFeaturedContent(featured)
                _operationStatus.value = "Featured song: ${music.title}"
                loadFeaturedContent()
            } ?: run {
                _operationStatus.value = "Error: Admin not loaded"
            }
        }
    }

    fun featureArtist(artist: User) {
        viewModelScope.launch {
            _currentAdmin.value?.let { admin ->
                // Use the local file path for the profile image if available
                val profileImageUrl = artist.profileImageUrl

                val featured = FeaturedContent(
                    id = UUID.randomUUID().toString(),
                    title = artist.name,
                    description = null,
                    imageUrl = profileImageUrl,
                    type = FeaturedType.ARTIST,
                    contentId = artist.id,
                    createdBy = admin.id,
                    featuredBy = admin.id,
                    position = 0
                )
                featuredContentRepository.insertFeaturedContent(featured)
                _operationStatus.value = "Featured artist: ${artist.name}"
                loadFeaturedContent()
            } ?: run {
                _operationStatus.value = "Error: Admin not loaded"
            }
        }
    }

    private fun loadFeaturedSongs() {
        viewModelScope.launch {
            featuredContentRepository.getAllFeaturedContent().collectLatest { contents ->
                val songIds = contents.filter { it.type == FeaturedType.SONG }.mapNotNull { it.contentId }
                val songs = songIds.mapNotNull { id -> musicRepository.getMusicById(id) }
                _featuredSongs.value = songs
            }
        }
    }

    private fun loadFeaturedArtists() {
        viewModelScope.launch {
            featuredContentRepository.getAllFeaturedContent().collectLatest { contents ->
                val artistIds = contents.filter { it.type == FeaturedType.ARTIST }.mapNotNull { it.contentId }
                val artists = artistIds.mapNotNull { id -> userRepository.getUserById(id) }
                _featuredArtists.value = artists
            }
        }
    }

    fun createFeaturedBanner(title: String, description: String, imageUrl: String, adminId: String) {
        viewModelScope.launch {
            val featured = FeaturedContent(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                imageUrl = imageUrl,
                type = FeaturedType.BANNER,
                contentId = null,
                createdBy = adminId,
                featuredBy = adminId,
                position = 0
            )
            featuredContentRepository.insertFeaturedContent(featured)
            _operationStatus.value = "Featured banner created"
            loadFeaturedContent()
        }
    }

    // New method to save banner image using ImagePickerHelper
    suspend fun saveBannerImage(context: Context, uri: Uri): String? {
        try {
            val fileName = "banner_${System.currentTimeMillis()}.jpg"
            return ImagePickerHelper.saveImageToInternalStorage(context, uri, fileName)
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error saving banner image: ${e.message}", e)
            _operationStatus.value = "Error saving image: ${e.message}"
            return null
        }
    }

    fun removeFeaturedContent(id: String) {
        viewModelScope.launch {
            featuredContentRepository.removeFeaturedContent(id)
            _operationStatus.value = "Removed featured content"
            loadFeaturedContent()
        }
    }

    fun makeArtist(userId: String) {
        viewModelScope.launch {
            userRepository.promoteUserToArtist(userId)
            _operationStatus.value = "User promoted to Artist"
            loadAllUsers()
        }
    }

    fun removeArtistFromFeatured(artistId: String) {
        viewModelScope.launch {
            val artistContent = _featuredContent.value.find {
                it.type == FeaturedType.ARTIST && it.contentId == artistId
            }
            artistContent?.let {
                featuredContentRepository.removeFeaturedContent(it.id)
                _operationStatus.value = "Removed featured artist"
                loadFeaturedContent()
            }
        }
    }

    fun removeFromFeatured(musicId: String) {
        viewModelScope.launch {
            val songContent = _featuredContent.value.find {
                it.type == FeaturedType.SONG && it.contentId == musicId
            }
            songContent?.let {
                featuredContentRepository.removeFeaturedContent(it.id)
                _operationStatus.value = "Removed featured song"
                loadFeaturedContent()
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }
}
