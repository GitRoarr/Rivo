package com.rivo.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _artists = MutableStateFlow<List<User>>(emptyList())
    val artists: StateFlow<List<User>> = _artists.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAllUsers()
        loadArtists()
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.getAllUsers().collectLatest { list ->
                    _users.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.getArtists().collectLatest { list ->
                    _artists.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserByEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getUserByEmail(email)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun updateUserProfile(
        fullName: String,
        bio: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = _currentUser.value
        if (currentUser != null) {
            viewModelScope.launch {
                _isLoading.value = true  // Set loading to true when the update starts
                try {
                    val result = userRepository.updateUserProfile(
                        email = currentUser.email,
                        fullName = fullName,
                        bio = bio
                    )

                    result.onSuccess { updatedUser ->
                        _currentUser.value = updatedUser
                        _isLoading.value = false  // Set loading to false when the update succeeds
                        onResult(true, null)  // Notify success
                    }.onFailure { error ->
                        _error.value = error.message
                        _isLoading.value = false  // Set loading to false when the update fails
                        onResult(false, error.message)  // Notify failure
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                    _isLoading.value = false  // Set loading to false in case of an exception
                    onResult(false, e.message)  // Notify failure with exception message
                }
            }
        } else {
            val msg = "No user is currently logged in"
            _error.value = msg
            _isLoading.value = false  // Set loading to false when no user is logged in
            onResult(false, msg)  // Notify failure if there's no logged-in user
        }
    }




}
