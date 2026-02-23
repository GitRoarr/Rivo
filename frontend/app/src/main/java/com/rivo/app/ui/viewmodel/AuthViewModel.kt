package com.rivo.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.data.repository.UserRepository
import com.rivo.app.utils.ImagePickerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@gmail\\.com$"
    )

    // Password validation pattern - at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    )

    val isLoggedIn: StateFlow<Boolean> = sessionManager.sessionFlow
        .map { it.isLoggedIn }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val userType: StateFlow<UserType> = sessionManager.sessionFlow
        .map { it.userType }
        .stateIn(viewModelScope, SharingStarted.Lazily, UserType.GUEST)  // Default to GUEST if no session

    val userEmail: StateFlow<String> = sessionManager.sessionFlow
        .map { it.email }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    init {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { currentSession ->
                if (currentSession.isLoggedIn) {
                    _currentUser.value = userRepository.getUserByEmail(currentSession.email)
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val result = userRepository.loginUser(email, password)

                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    sessionManager.createSession(user)
                    _currentUser.value = user
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    fun getPasswordErrorMessage(password: String): String {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("Password must be at least 8 characters long")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one number")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Password must contain at least one special character")
        }

        return errors.joinToString("\n")
    }

    fun register(fullName: String, email: String, password: String, userType: UserType) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            try {
                // Validate email format
                if (!validateEmail(email)) {
                    _registerState.value = RegisterState.Error("Please enter a valid Gmail address")
                    return@launch
                }

                // Validate password strength
                if (!validatePassword(password)) {
                    _registerState.value = RegisterState.Error(getPasswordErrorMessage(password))
                    return@launch
                }

                val result = userRepository.registerUser(
                    fullName = fullName,
                    name = fullName.replace(" ", "_").lowercase(),
                    email = email,
                    password = password,
                    userType = userType // directly UserType, no .name
                )

                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    sessionManager.createSession(user)
                    _currentUser.value = user
                    _registerState.value = RegisterState.Success("Congratulations! Your account has been successfully created. Welcome to Arif Music!")
                } else {
                    _registerState.value = RegisterState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun deleteAccount(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading

            try {
                val result = userRepository.deleteAccount(userId)

                if (result.isSuccess) {
                    // Clear the session
                    sessionManager.clearSession()
                    _currentUser.value = null
                    _deleteAccountState.value = DeleteAccountState.Success
                    onComplete()
                } else {
                    _deleteAccountState.value = DeleteAccountState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to delete account"
                    )
                }
            } catch (e: Exception) {
                _deleteAccountState.value = DeleteAccountState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading

            try {
                val user = userRepository.getUserByEmail(email)

                if (user != null) {
                    delay(1500) // Simulate network delay
                    val newPassword = generateStrongPassword()
                    userRepository.updateUserPassword(email, newPassword)
                    _forgotPasswordState.value = ForgotPasswordState.Success
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.Error("No account found with this email address")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(fullName: String, bio: String?) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = userRepository.updateUserProfile(user.email, fullName, bio)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }


    fun updateProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch

                if (uri.toString().startsWith("file://")) {
                    val path = uri.toString()
                    userRepository.updateProfileImage(user.email, path)
                    _currentUser.value = userRepository.getUserByEmail(user.email)
                }
                else {
                    val session = sessionManager.sessionFlow.first()
                    val userId = if (session.userId.isNotEmpty()) session.userId else user.id
                    val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
                    val localPath = ImagePickerHelper.saveImageToInternalStorage(context, uri, fileName)

                    if (localPath != null) {
                        val fileUri = "file://$localPath"
                        userRepository.updateProfileImage(user.email, fileUri)
                        _currentUser.value = userRepository.getUserByEmail(user.email)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating profile image: ${e.message}", e)
            }
        }
    }

    fun updateCoverImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch

                if (uri.toString().startsWith("file://")) {
                    val path = uri.toString()
                    userRepository.updateCoverImage(user.email, path)
                    _currentUser.value = userRepository.getUserByEmail(user.email)
                }
                else {
                    val session = sessionManager.sessionFlow.first()
                    val userId = if (session.userId.isNotEmpty()) session.userId else user.id
                    val fileName = "cover_${userId}_${System.currentTimeMillis()}.jpg"
                    val localPath = ImagePickerHelper.saveImageToInternalStorage(context, uri, fileName)

                    if (localPath != null) {
                        val fileUri = "file://$localPath"
                        userRepository.updateCoverImage(user.email, fileUri)
                        _currentUser.value = userRepository.getUserByEmail(user.email)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating cover image: ${e.message}", e)
            }
        }
    }

    fun approveArtist(email: String, approved: Boolean) {
        viewModelScope.launch {
            userRepository.approveArtist(email, approved)
        }
    }

    fun generateStrongPassword(): String {
        val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lower = "abcdefghijkmnopqrstuvwxyz"
        val digits = "23456789"
        val special = "!@#$%^&*"
        val allChars = upper + lower + digits + special

        val passwordChars = mutableListOf<Char>()

        passwordChars += upper.random()
        passwordChars += lower.random()
        passwordChars += digits.random()
        passwordChars += special.random()

        repeat(6) {
            passwordChars += allChars.random()
        }

        return passwordChars.shuffled().joinToString("")
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _currentUser.value = null
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
}

// Sealed classes
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}


sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}