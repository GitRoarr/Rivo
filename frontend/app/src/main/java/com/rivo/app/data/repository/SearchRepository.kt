package com.rivo.app.data.repository

import com.rivo.app.data.local.MusicDao
import com.rivo.app.data.local.UserDao
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val musicDao: MusicDao,
    private val userDao: UserDao
) {
    fun searchMusic(query: String): Flow<List<Music>> =
        musicDao.searchMusic(query)

    fun searchArtists(query: String): Flow<List<User>> =
        userDao.searchUsersByType(query, UserType.ARTIST)

    fun searchAll(query: String): Flow<Pair<List<Music>, List<User>>> =
        combine(searchMusic(query), searchArtists(query)) { music, artists ->
            music to artists
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
