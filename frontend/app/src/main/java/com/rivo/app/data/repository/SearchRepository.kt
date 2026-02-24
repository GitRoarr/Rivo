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
    private val _searchResults = MutableStateFlow<Pair<List<Music>, List<User>>>(emptyList<Music>() to emptyList<User>())
    val searchResults: StateFlow<Pair<List<Music>, List<User>>> = _searchResults.asStateFlow()

    fun searchAll(query: String): Flow<Pair<List<Music>, List<User>>> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                if (query.isBlank()) {
                    _searchResults.value = emptyList<Music>() to emptyList<User>()
                    return@launch
                }
                val response = apiService.searchAll(query)
                if (response.isSuccessful) {
                    val body = response.body()
                    _searchResults.value = (body?.music ?: emptyList()) to (body?.artists ?: emptyList())
                }
            } catch (e: Exception) {
                Log.e("SearchRepository", "Search failed: ${e.message}")
            }
        }
        return searchResults
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
