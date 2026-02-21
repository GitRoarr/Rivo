package com.rivo.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.rivo.app.data.model.Session
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "arif_music_prefs",
        Context.MODE_PRIVATE
    )

    private val _sessionFlow = MutableStateFlow(getCurrentSession())
    val sessionFlow: StateFlow<Session> = _sessionFlow

    fun createSession(user: User) {
        Log.d("SessionManager", "Creating session for user: ${user.email}")
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, user.id)
            putString(KEY_EMAIL, user.email)
            putString(KEY_USER_TYPE, user.userType.name)
            apply()
        }
        _sessionFlow.value = getCurrentSession()
    }

    fun clearSession() {
        Log.d("SessionManager", "Clearing session")
        with(sharedPreferences.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putString(KEY_USER_ID, "")
            putString(KEY_EMAIL, "")
            putString(KEY_USER_TYPE, UserType.GUEST.name)
            putString(KEY_TOKEN, "")
            apply()
        }
        _sessionFlow.value = getCurrentSession()
    }

    fun saveToken(token: String) {
        Log.d("SessionManager", "Saving token: ${token.take(10)}...")
        with(sharedPreferences.edit()) {
            putString(KEY_TOKEN, token)
            apply()
        }
        // Update the session flow with the new token
        _sessionFlow.value = getCurrentSession()
    }

    fun getToken(): String {
        return sharedPreferences.getString(KEY_TOKEN, "") ?: ""
    }

    private fun getCurrentSession(): Session {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val userId = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
        val email = sharedPreferences.getString(KEY_EMAIL, "") ?: ""
        val userTypeStr = sharedPreferences.getString(KEY_USER_TYPE, UserType.GUEST.name) ?: UserType.GUEST.name
        val userType = try {
            UserType.valueOf(userTypeStr)
        } catch (e: Exception) {
            UserType.GUEST
        }
        val token = sharedPreferences.getString(KEY_TOKEN, "") ?: ""

        return Session(
            isLoggedIn = isLoggedIn,
            userId = userId,
            email = email,
            userType = userType,
            token = token
        )
    }

    suspend fun getCurrentUser(): Session {
        return sessionFlow.first()
    }

    suspend fun getCurrentUserId(): String {
        return sessionFlow.first().userId
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_TOKEN = "token"
    }
}
