package com.rivo.app.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.rivo.app.data.model.Music
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.ExploreResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    private val _allMusic = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
    val allMusic: kotlinx.coroutines.flow.StateFlow<List<Music>> = _allMusic.asStateFlow()

    fun getAllMusic(): Flow<List<Music>> {
        return allMusic
    }

    suspend fun refreshMusic() = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllMusic()
            if (response.isSuccessful) {
                response.body()?.let { musicList ->
                    _allMusic.value = musicList
                }
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Refresh music failed: ${e.message}")
        }
    }

    suspend fun refreshAllMusicAdmin(): Result<List<Music>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllMusicAdmin()
            if (response.isSuccessful && response.body() != null) {
                val music = response.body()!!
                _allMusic.value = music
                return@withContext Result.success(music)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh admin music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun refreshPendingMusic(): Result<List<Music>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPendingMusic()
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh pending music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun getMusicByGenre(genre: String): Result<List<Music>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMusicByGenre(genre)
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!)
            } else {
                return@withContext Result.failure(Exception("Failed to get music by genre"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun incrementPlayCountIfFirstTime(musicId: String): Boolean {
        return try {
            val userId = sessionManager.getCurrentUser().userId.takeIf { it.isNotBlank() }
            val request = com.rivo.app.data.remote.IncrementPlayRequest(userId = userId)
            val response = apiService.incrementMusicPlay(musicId, request)
            if (response.isSuccessful) {
                val counted = response.body()?.counted ?: false
                Log.d("MusicRepository", "Play count increment result: counted=$counted")
                counted
            } else {
                Log.e("MusicRepository", "Failed to increment play count: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to sync play count: ${e.message}")
            false
        }
    }

    suspend fun getFavoriteMusic(): Flow<List<Music>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLikedSongs()
                if (response.isSuccessful) {
                    flow.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to fetch liked songs: ${e.message}")
            }
        }
        return flow
    }

    suspend fun toggleFavorite(musicId: String, isFavorite: Boolean) {
        try {
            if (isFavorite) {
                apiService.likeMusic(musicId)
            } else {
                apiService.unlikeMusic(musicId)
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to sync favorite to backend: ${e.message}")
            throw e
        }
    }

    suspend fun getMusicById(musicId: String): Music? {
        return try {
            val response = apiService.getMusicById(musicId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    fun getArtistMusic(artistId: String): Flow<List<Music>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getMusicByArtist(artistId)
                if (response.isSuccessful) {
                    flow.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to fetch artist music: ${e.message}")
            }
        }
        return flow
    }

    suspend fun refreshArtistMusic(artistId: String): Result<List<Music>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getMusicByArtist(artistId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch artist music"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementPlayCount(musicId: String) {
        try {
            val userId = sessionManager.getCurrentUser().userId.takeIf { it.isNotBlank() }
            val request = com.rivo.app.data.remote.IncrementPlayRequest(userId = userId)
            apiService.incrementMusicPlay(musicId, request)
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to increment play count: ${e.message}")
        }
    }

    fun getNewReleases(): Flow<List<Music>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getExploreData()
                if (response.isSuccessful) {
                    flow.value = response.body()?.newReleases ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to fetch new releases: ${e.message}")
            }
        }
        return flow
    }

    fun getTrendingMusic(): Flow<List<Music>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getExploreData()
                if (response.isSuccessful) {
                    flow.value = response.body()?.trendingMusic ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to fetch trending music: ${e.message}")
            }
        }
        return flow
    }

    suspend fun deleteMusic(musicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMusic(musicId)
            if (response.isSuccessful) {
                refreshMusic()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to delete music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    fun getPendingApprovalMusic(): Flow<List<Music>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<Music>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getPendingMusic()
                if (response.isSuccessful) {
                    flow.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to fetch pending music: ${e.message}")
            }
        }
        return flow
    }

    suspend fun approveMusic(musicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveMusic(musicId)
            if (response.isSuccessful) {
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to approve music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun rejectMusic(musicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectMusic(musicId)
            if (response.isSuccessful) {
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to reject music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun uploadMusic(
        title: String,
        genre: String,
        album: String?,
        duration: Long,
        audioPath: String,
        coverImagePath: String?
    ): Result<Music> = withContext(Dispatchers.IO) {
        var tempAudioFile: File? = null
        return@withContext try {
            val audioUri = Uri.parse(audioPath)
            val audioFile = if (audioUri.scheme == "content") {
                val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.mp3")
                context.contentResolver.openInputStream(audioUri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempAudioFile = tempFile
                tempFile
            } else {
                File(audioPath.replace("file://", ""))
            }

            if (!audioFile.exists()) return@withContext Result.failure(Exception("Audio file not found"))

            val audioRequestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestFile)

            val coverPart = coverImagePath?.let { path ->
                val file = File(path.replace("file://", ""))
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("coverImage", file.name, requestFile)
                } else null
            }

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val genrePart = genre.toRequestBody("text/plain".toMediaTypeOrNull())
            val albumPart = album?.toRequestBody("text/plain".toMediaTypeOrNull())
            val durationPart = duration.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadMusic(
                audio = audioPart,
                coverImage = coverPart,
                title = titlePart,
                genre = genrePart,
                album = albumPart,
                duration = durationPart
            )

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempAudioFile?.delete()
        }
    }

    fun getAudioDuration(context: Context, audioUri: Uri?): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, audioUri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }

    suspend fun getExploreData(): Result<ExploreResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getExploreData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch explore data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<com.rivo.app.data.remote.MusicCategory>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getExploreData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()?.categories ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
