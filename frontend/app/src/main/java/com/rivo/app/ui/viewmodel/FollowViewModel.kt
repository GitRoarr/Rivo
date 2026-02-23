package com.rivo.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.FollowRepository
import com.rivo.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val followRepository: FollowRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val getFollowersCount: StateFlow<Int> = _followersCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val getFollowingCount: StateFlow<Int> = _followingCount.asStateFlow()

    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers.asStateFlow()

    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val session = sessionManager.getCurrentUser()
                if (session.isLoggedIn && session.userId.isNotBlank()) {
                    Log.d("FollowViewModel", "Initializing and syncing follows for ${session.userId}")
                    followRepository.syncFollows(session.userId)
                }
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error in init sync: ${e.message}")
            }
        }
    }

    // Cache for artist follow status
    private val followStatusCache = mutableMapOf<String, MutableStateFlow<Boolean>>()

    // Cache for artist follower counts
    private val followerCountCache = mutableMapOf<String, MutableStateFlow<Int>>()

    // Check follow status for an artist
    fun checkFollowStatus(artistId: String) {
        viewModelScope.launch {
            try {
                val currentUser = sessionManager.getCurrentUser()
                if (currentUser != null) {
                    _isFollowing.value = followRepository.isFollowing(currentUser.email, artistId)
                    Log.d("FollowViewModel", "Follow status for $artistId: ${_isFollowing.value}")
                }
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error checking follow status: ${e.message}", e)
                _error.value = "Failed to check follow status: ${e.message}"
            }
        }
    }

    // Toggle follow/unfollow action
    fun toggleFollow(artistId: String) {
        viewModelScope.launch {
            try {
                val session = sessionManager.getCurrentUser()

                if (!session.isLoggedIn || session.userId.isBlank()) {
                    Log.e("FollowViewModel", "Cannot toggle follow: user not logged in")
                    _error.value = "You need to be logged in to follow artists"
                    return@launch
                }

                // Prevent an artist from following themself
                if (session.userId == artistId) {
                    Log.e("FollowViewModel", "User attempted to follow themself: $artistId")
                    _error.value = "You cannot follow yourself"
                    return@launch
                }

                val currentUserId = session.email
                if (currentUserId.isNotBlank()) {
                    if (_isFollowing.value) {
                        Log.d("FollowViewModel", "Unfollowing artist: $artistId")
                        followRepository.unfollowArtist(currentUserId, artistId)
                        _isFollowing.value = false
                        _followersCount.value = (_followersCount.value - 1).coerceAtLeast(0)

                        // Update cache
                        followStatusCache[artistId]?.value = false
                        followerCountCache[artistId]?.value = (followerCountCache[artistId]?.value ?: 1) - 1
                    } else {
                        Log.d("FollowViewModel", "Following artist: $artistId")
                        followRepository.followArtist(currentUserId, artistId)
                        _isFollowing.value = true
                        _followersCount.value += 1

                        // Update cache
                        followStatusCache[artistId]?.value = true
                        followerCountCache[artistId]?.value = (followerCountCache[artistId]?.value ?: 0) + 1
                    }
                } else {
                    Log.e("FollowViewModel", "Cannot toggle follow: current user email is blank")
                    _error.value = "You need to be logged in to follow artists"
                }
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error toggling follow: ${e.message}", e)
                _error.value = "Failed to update follow status: ${e.message}"
            }
        }
    }

    // Load followers count for an artist
    fun loadFollowersCount(artistId: String) {
        viewModelScope.launch {
            try {
                val count = followRepository.getFollowersCount(artistId)
                Log.d("FollowViewModel", "Followers count for $artistId: $count")
                _followersCount.value = count

                // Update cache
                getOrCreateFollowerCountFlow(artistId).value = count
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error loading followers count: ${e.message}", e)
                _error.value = "Failed to load followers count: ${e.message}"
            }
        }
    }

    // Load following count for a user
    fun loadFollowingCount(userId: String) {
        viewModelScope.launch {
            try {
                _followingCount.value = followRepository.getFollowingCount(userId)
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error loading following count: ${e.message}", e)
                _error.value = "Failed to load following count: ${e.message}"
            }
        }
    }

    // Load followed artists for a user
    fun loadFollowedArtists(userId: String) {
        viewModelScope.launch {
            try {
                _following.value = followRepository.getFollowing(userId)
                Log.d("FollowViewModel", "Loaded ${_following.value.size} followed artists")
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error loading followed artists: ${e.message}", e)
                _error.value = "Failed to load followed artists: ${e.message}"
            }
        }
    }

    // Follow an artist (direct method)
    suspend fun followArtist(artistId: String) {
        try {
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser != null) {
                followRepository.followArtist(currentUser.email, artistId)

                // Update cache
                getOrCreateFollowStatusFlow(artistId).value = true
                getOrCreateFollowerCountFlow(artistId).value = (getOrCreateFollowerCountFlow(artistId).value) + 1

                // Refresh followed artists list
                loadFollowedArtists(currentUser.email)
            } else {
                _error.value = "You need to be logged in to follow artists"
            }
        } catch (e: Exception) {
            Log.e("FollowViewModel", "Error following artist: ${e.message}", e)
            _error.value = "Failed to follow artist: ${e.message}"
            throw e
        }
    }

    // Unfollow an artist (direct method)
    suspend fun unfollowArtist(artistId: String) {
        try {
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser != null) {
                followRepository.unfollowArtist(currentUser.email, artistId)

                // Update cache
                getOrCreateFollowStatusFlow(artistId).value = false
                getOrCreateFollowerCountFlow(artistId).value = (getOrCreateFollowerCountFlow(artistId).value - 1).coerceAtLeast(0)

                // Refresh followed artists list
                loadFollowedArtists(currentUser.email)
            } else {
                _error.value = "You need to be logged in to unfollow artists"
            }
        } catch (e: Exception) {
            Log.e("FollowViewModel", "Error unfollowing artist: ${e.message}", e)
            _error.value = "Failed to unfollow artist: ${e.message}"
            throw e
        }
    }

    // Get follow status for an artist (for UI components)
    fun isFollowingArtist(artistId: String): StateFlow<Boolean> {
        val followStatusFlow = getOrCreateFollowStatusFlow(artistId)

        // Load the actual status if not already loaded
        viewModelScope.launch {
            try {
                val currentUser = sessionManager.getCurrentUser()
                if (currentUser != null) {
                    val isFollowing = followRepository.isFollowing(currentUser.email, artistId)
                    followStatusFlow.value = isFollowing
                }
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error checking follow status: ${e.message}", e)
            }
        }

        return followStatusFlow
    }

    fun getArtistFollowerCount(artistId: String): StateFlow<Int> {
        val followerCountFlow = getOrCreateFollowerCountFlow(artistId)

        // Load the actual count if not already loaded
        viewModelScope.launch {
            try {
                val count = followRepository.getFollowersCount(artistId)
                followerCountFlow.value = count
            } catch (e: Exception) {
                Log.e("FollowViewModel", "Error loading follower count: ${e.message}", e)
            }
        }

        return followerCountFlow
    }

    // Helper method to get or create a follow status flow for an artist
    private fun getOrCreateFollowStatusFlow(artistId: String): MutableStateFlow<Boolean> {
        return followStatusCache.getOrPut(artistId) {
            MutableStateFlow(false)
        }
    }

    // Helper method to get or create a follower count flow for an artist
    private fun getOrCreateFollowerCountFlow(artistId: String): MutableStateFlow<Int> {
        return followerCountCache.getOrPut(artistId) {
            MutableStateFlow(0)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
