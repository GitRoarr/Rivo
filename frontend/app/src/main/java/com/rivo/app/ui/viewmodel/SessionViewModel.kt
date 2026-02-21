package com.rivo.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.Session
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCurrentSession()
    }

    private fun loadCurrentSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                sessionManager.sessionFlow.collectLatest { session ->
                    _currentSession.value = session

                    // Load user data if session exists
                    if (session != null) {
                        val user = userRepository.getUserByEmail(session.email)
                        _currentUser.value = user
                        Log.d("SessionViewModel", "Loaded user: ${user?.name}")
                    } else {
                        _currentUser.value = null
                    }

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error loading session: ${e.message}", e)
                _error.value = "Failed to load session: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
                _currentUser.value = null
                _currentSession.value = null
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error during logout: ${e.message}", e)
                _error.value = "Failed to logout: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
