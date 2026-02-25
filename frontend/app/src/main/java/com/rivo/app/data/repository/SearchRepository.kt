package com.rivo.app.data.repository

import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.rivo.app.data.remote.ApiService

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun searchAll(query: String): Result<Pair<List<Music>, List<User>>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList<Music>() to emptyList<User>())
            }
            val response = apiService.searchAll(query)
            if (response.isSuccessful) {
                val body = response.body()
                val results = (body?.music ?: emptyList()) to (body?.artists ?: emptyList())
                Result.success(results)
            } else {
                Result.failure(Exception("Search failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Search failed: ${e.message}")
            Result.failure(e)
        }
    }

    private val inMemoryHistory = mutableMapOf<String, MutableList<String>>()

    fun getRecentSearches(userId: String): List<String> =
        inMemoryHistory[userId]?.toList().orEmpty()

    fun saveSearchQuery(userId: String, query: String) {
        val list = inMemoryHistory.getOrPut(userId) { mutableListOf() }
        if (query.isNotBlank()) {
            list.remove(query)
            list.add(0, query)
            if (list.size > 20) list.removeAt(list.lastIndex)
        }
    }

    fun clearRecentSearches(userId: String) {
        inMemoryHistory.remove(userId)
    }
}
