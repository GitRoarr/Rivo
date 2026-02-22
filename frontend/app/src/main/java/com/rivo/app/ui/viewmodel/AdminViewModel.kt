package com.rivo.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.data.remote.AdminStatsResponse
import com.rivo.app.data.repository.FeaturedContentRepository
import com.rivo.app.data.repository.MusicRepository
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.data.repository.StatsRepository
import com.rivo.app.data.repository.UserRepository
import com.rivo.app.utils.ImagePickerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val featuredContentRepository: FeaturedContentRepository,
    private val musicRepository: MusicRepository,
    private val userRepository: UserRepository,
    private val statsRepository: StatsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentAdmin = MutableStateFlow<User?>(null)
    val currentAdmin: StateFlow<User?> = _currentAdmin

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _allMusic = MutableStateFlow<List<Music>>(emptyList())
    val allMusic: StateFlow<List<Music>> = _allMusic

    private val _platformStats = MutableStateFlow<AdminStatsResponse?>(null)
    val platformStats: StateFlow<AdminStatsResponse?> = _platformStats

    private val _featuredSongs = MutableStateFlow<List<Music>>(emptyList())
    val featuredSongs: StateFlow<List<Music>> = _featuredSongs

    private val _featuredArtists = MutableStateFlow<List<User>>(emptyList())
    val featuredArtists: StateFlow<List<User>> = _featuredArtists

    private val _pendingVerifications = MutableStateFlow<List<User>>(emptyList())
    val pendingVerifications: StateFlow<List<User>> = _pendingVerifications

    private val _pendingMusic = MutableStateFlow<List<Music>>(emptyList())
    val pendingMusic: StateFlow<List<Music>> = _pendingMusic

    private val _featuredContent = MutableStateFlow<List<FeaturedContent>>(emptyList())
    val featuredContent: StateFlow<List<FeaturedContent>> = _featuredContent

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            val session = sessionManager.sessionFlow.first()
            if (session.userType == UserType.ADMIN) {
                val admin = userRepository.getUserByEmail(session.email)
                _currentAdmin.value = admin
            }
            refreshAllData()
            observeData()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            userRepository.getAllUsers().collectLatest { users ->
                _allUsers.value = users
                _pendingVerifications.value = users.filter { it.verificationStatus == VerificationStatus.PENDING }
            }
        }

        viewModelScope.launch {
            musicRepository.getAllMusic().collectLatest { musics ->
                _allMusic.value = musics
                _pendingMusic.value = musics.filter { it.approvalStatus == MusicApprovalStatus.PENDING }
            }
        }

        viewModelScope.launch {
            featuredContentRepository.getAllFeaturedContent().collectLatest { contents ->
                _featuredContent.value = contents
            }
        }

        // Keep featuredSongs and featuredArtists in sync
        viewModelScope.launch {
            combine(_allMusic, _featuredContent) { musics, featured ->
                musics.filter { m -> featured.any { it.type == FeaturedType.SONG && it.contentId == m.id } }
            }.collectLatest {
                _featuredSongs.value = it
            }
        }

        viewModelScope.launch {
            combine(_allUsers, _featuredContent) { users, featured ->
                users.filter { u -> featured.any { it.type == FeaturedType.ARTIST && it.contentId == u.id } }
            }.collectLatest {
                _featuredArtists.value = it
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.refreshAllUsers()
                musicRepository.refreshAllMusicAdmin()
                musicRepository.refreshPendingMusic()
                userRepository.refreshPendingVerifications()
                featuredContentRepository.refreshFeaturedContent()
                
                statsRepository.getAdminStats().onSuccess { stats ->
                    _platformStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error refreshing data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun suspendUser(userId: String) {
        viewModelScope.launch {
            val result = userRepository.suspendUser(userId, true)
            if (result.isSuccess) {
                _operationStatus.value = "User suspended"
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to suspend user"
            }
        }
    }

    fun unsuspendUser(userId: String) {
        viewModelScope.launch {
            val result = userRepository.suspendUser(userId, false)
            if (result.isSuccess) {
                _operationStatus.value = "User unsuspended"
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to unsuspend user"
            }
        }
    }

    fun makeAdmin(userId: String) {
        viewModelScope.launch {
            val result = userRepository.promoteUserToAdmin(userId)
            if (result.isSuccess) {
                _operationStatus.value = "User promoted to Admin"
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to promote user"
            }
        }
    }

    fun makeArtist(userId: String) {
        viewModelScope.launch {
            val result = userRepository.promoteUserToArtist(userId)
            if (result.isSuccess) {
                _operationStatus.value = "User promoted to Artist"
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to promote user"
            }
        }
    }

    fun approveMusic(musicId: String) {
        viewModelScope.launch {
            val result = musicRepository.approveMusic(musicId)
            if (result.isSuccess) {
                _operationStatus.value = "Music approved"
                musicRepository.refreshPendingMusic()
                musicRepository.refreshAllMusicAdmin()
            } else {
                _operationStatus.value = "Failed to approve music"
            }
        }
    }

    fun rejectMusic(musicId: String) {
        viewModelScope.launch {
            val result = musicRepository.rejectMusic(musicId)
            if (result.isSuccess) {
                _operationStatus.value = "Music rejected"
                musicRepository.refreshPendingMusic()
            } else {
                _operationStatus.value = "Failed to reject music"
            }
        }
    }

    fun deleteMusic(musicId: String) {
        viewModelScope.launch {
            val result = musicRepository.deleteMusic(musicId)
            if (result.isSuccess) {
                _operationStatus.value = "Music deleted"
                musicRepository.refreshAllMusicAdmin()
            } else {
                _operationStatus.value = "Failed to delete music"
            }
        }
    }

    fun approveVerification(userId: String) {
        viewModelScope.launch {
            val result = userRepository.approveUserVerification(userId)
            if (result.isSuccess) {
                _operationStatus.value = "Verification approved"
                userRepository.refreshPendingVerifications()
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to approve verification"
            }
        }
    }

    fun rejectVerification(userId: String) {
        viewModelScope.launch {
            val result = userRepository.rejectUserVerification(userId)
            if (result.isSuccess) {
                _operationStatus.value = "Verification rejected"
                userRepository.refreshPendingVerifications()
                userRepository.refreshAllUsers()
            } else {
                _operationStatus.value = "Failed to reject verification"
            }
        }
    }

    fun featureMusic(music: Music) {
        viewModelScope.launch {
            _currentAdmin.value?.let { admin ->
                val result = featuredContentRepository.createFeaturedContent(
                    title = music.title,
                    description = music.artist,
                    type = FeaturedType.SONG,
                    contentId = music.id,
                    imageUrl = music.artworkUri
                )
                if (result.isSuccess) {
                    _operationStatus.value = "Featured song: ${music.title}"
                    featuredContentRepository.refreshFeaturedContent()
                } else {
                    _operationStatus.value = "Failed to feature music"
                }
            }
        }
    }

    fun featureArtist(artist: User) {
        viewModelScope.launch {
            _currentAdmin.value?.let { admin ->
                val result = featuredContentRepository.createFeaturedContent(
                    title = artist.name,
                    description = "Featured Artist",
                    type = FeaturedType.ARTIST,
                    contentId = artist.id,
                    imageUrl = artist.profileImageUrl
                )
                if (result.isSuccess) {
                    _operationStatus.value = "Featured artist: ${artist.name}"
                    featuredContentRepository.refreshFeaturedContent()
                } else {
                    _operationStatus.value = "Failed to feature artist"
                }
            }
        }
    }

    fun createFeaturedBanner(title: String, description: String, imageUrl: String, adminId: String) {
        viewModelScope.launch {
            val result = featuredContentRepository.createFeaturedContent(
                title = title,
                description = description,
                type = FeaturedType.BANNER,
                contentId = null,
                imageUrl = imageUrl
            )
            if (result.isSuccess) {
                _operationStatus.value = "Featured banner created"
                featuredContentRepository.refreshFeaturedContent()
            } else {
                _operationStatus.value = "Failed to create banner"
            }
        }
    }

    fun removeFromFeatured(musicId: String) {
        viewModelScope.launch {
            _featuredContent.value.find { it.type == FeaturedType.SONG && it.contentId == musicId }?.let {
                removeFeaturedContent(it.id)
            }
        }
    }

    fun removeArtistFromFeatured(artistId: String) {
        viewModelScope.launch {
            _featuredContent.value.find { it.type == FeaturedType.ARTIST && it.contentId == artistId }?.let {
                removeFeaturedContent(it.id)
            }
        }
    }

    fun removeFeaturedContent(id: String) {
        viewModelScope.launch {
            val result = featuredContentRepository.removeFeaturedContent(id)
            if (result.isSuccess) {
                _operationStatus.value = "Removed featured content"
                featuredContentRepository.refreshFeaturedContent()
            } else {
                _operationStatus.value = "Failed to remove featured content"
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }
}
