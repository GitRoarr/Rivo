package com.rivo.app.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.rivo.app.data.local.UserDao
import com.rivo.app.data.local.MusicDao
import com.rivo.app.data.local.MusicPlayedDao
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.data.model.MusicPlayed
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.ExploreResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val musicDao: MusicDao,
    private val userDao: UserDao,
    private val musicPlayedDao: MusicPlayedDao,
    private val sessionManager: SessionManager,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    fun getAllMusic(): Flow<List<Music>> {
        // Trigger a remote sync in the background
        refreshMusic()
        return musicDao.getAllMusic()
    }

    private fun refreshMusic() {
        // Use a safe scope for background sync
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = apiService.getAllMusic()
                if (response.isSuccessful) {
                    response.body()?.let { musicList ->
                        musicDao.insertAllMusic(musicList)
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicRepository", "Background sync failed for music: ${e.message}")
            }
        }
    }

    suspend fun refreshAllMusicAdmin(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllMusicAdmin()
            if (response.isSuccessful && response.body() != null) {
                musicDao.insertAllMusic(response.body()!!)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh admin music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun refreshPendingMusic(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPendingMusic()
            if (response.isSuccessful && response.body() != null) {
                musicDao.insertAllMusic(response.body()!!)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh pending music"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun incrementPlayCountIfFirstTime(musicId: String) {
        val userId = sessionManager.getCurrentUserId()
        val hasPlayed = musicPlayedDao.hasUserPlayedMusic(userId, musicId) > 0
        if (!hasPlayed) {
            musicDao.incrementPlayCount(musicId)
            musicPlayedDao.insertMusicPlayed(MusicPlayed(userId = userId, musicId = musicId))
            // Sync play count to server
            try {
                apiService.incrementMusicPlay(musicId)
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to sync play count: ${e.message}")
            }
        }
    }

    suspend fun getFavoriteMusic(): Flow<List<Music>> {
        val userId = sessionManager.getCurrentUserId()
        return musicDao.getFavoriteMusic(userId)
    }

    suspend fun toggleFavorite(musicId: String, isFavorite: Boolean) {
        musicDao.updateFavoriteStatus(musicId, isFavorite)
    }

    suspend fun getMusicById(musicId: String): Music? {
        return musicDao.getMusicById(musicId)
    }

    fun getArtistMusic(artistId: String): Flow<List<Music>> {
        return musicDao.getArtistMusic(artistId)
    }

    suspend fun refreshArtistMusic(artistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getMusicByArtist(artistId)
            if (response.isSuccessful && response.body() != null) {
                musicDao.insertAllMusic(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch artist music"))
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "refreshArtistMusic exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun incrementPlayCount(musicId: String) {
        musicDao.incrementPlayCount(musicId)
    }

    fun getNewReleases(): Flow<List<Music>> {
        return musicDao.getNewReleases()
    }

    fun getTrendingMusic(): Flow<List<Music>> {
        return musicDao.getTrendingMusic()
    }

    suspend fun insertMusic(music: Music) {
        musicDao.insertMusic(music)
        Log.d("UploadDebug", "Music inserted: ${music.title}")
    }

    suspend fun deleteMusic(musicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMusic(musicId)
            if (response.isSuccessful) {
                val music = musicDao.getMusicById(musicId)
                music?.let { musicDao.deleteMusic(it) }
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to delete music"))
            }
        } catch (e: Exception) {
            val music = musicDao.getMusicById(musicId)
            music?.let { musicDao.deleteMusic(it) }
            return@withContext Result.success(Unit)
        }
    }

    fun getPendingApprovalMusic(): Flow<List<Music>> {
        return musicDao.getMusicByApprovalStatus(MusicApprovalStatus.PENDING)
    }

    suspend fun approveMusic(musicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveMusic(musicId)
            if (response.isSuccessful) {
                val music = musicDao.getMusicById(musicId)
                music?.let {
                    musicDao.insertMusic(it.copy(approvalStatus = MusicApprovalStatus.APPROVED))
                }
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
                val music = musicDao.getMusicById(musicId)
                music?.let {
                    musicDao.insertMusic(it.copy(approvalStatus = MusicApprovalStatus.REJECTED))
                }
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
                val music = response.body()
                if (music != null) {
                    Result.success(music)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "uploadMusic exception: ${e.message}", e)
            Result.failure(e)
        } finally {
            tempAudioFile?.let {
                if (it.exists()) it.delete()
            }
        }
    }

    fun getAudioDuration(context: Context, audioUri: Uri?): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, audioUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }

    suspend fun getExploreData(): Result<ExploreResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Always treat MongoDB (backend) as the source of truth
            val response = apiService.getExploreData()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                // Best-effort cache in Room, but never fail the network result
                try {
                    musicDao.insertAllMusic(data.trendingMusic)
                    musicDao.insertAllMusic(data.newReleases)
                    musicDao.insertAllMusic(data.featuredMusic)
                } catch (cacheError: Exception) {
                    Log.e("MusicRepository", "Caching explore data failed: ${cacheError.message}", cacheError)
                }

                Result.success(data)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch explore data"))
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "getExploreData exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
