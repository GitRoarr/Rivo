package com.rivo.app.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

object SimpleMediaAccessHelper {
    private const val TAG = "SimpleMediaAccessHelper"


    fun getPlayableUri(context: Context, pathOrUri: String?): Uri? {
        if (pathOrUri.isNullOrEmpty()) return null

        try {
            val originalUri = when {
                pathOrUri.startsWith("content://") -> pathOrUri.toUri()
                pathOrUri.startsWith("/") -> File(pathOrUri).toUri()
                pathOrUri.startsWith("file://") -> pathOrUri.toUri()
                pathOrUri.startsWith("http") -> pathOrUri.toUri()
                else -> "file://$pathOrUri".toUri()
            }


            if (originalUri.scheme == "file") {
                val file = File(originalUri.path ?: "")
                if (file.exists() && file.canRead()) {
                    Log.d(TAG, "File exists and is readable: ${file.absolutePath}")
                    return originalUri
                } else {
                    Log.d(TAG, "File does not exist or is not readable: ${file.absolutePath}")
                }
            }

            val fileName = getFileNameFromUri(context, originalUri)
            val existingCopy = findExistingCopy(context, fileName)
            if (existingCopy != null) {
                Log.d(TAG, "Using existing local copy: ${existingCopy.absolutePath}")
                return Uri.fromFile(existingCopy)
            }

            if (originalUri.scheme == "content") {
                try {
                    val localCopy = createLocalCopy(context, originalUri)
                    if (localCopy != null) {
                        Log.d(TAG, "Created local copy: ${localCopy.absolutePath}")
                        return Uri.fromFile(localCopy)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating local copy: ${e.message}", e)
                }
            }

            Log.d(TAG, "Falling back to original URI: $originalUri")
            return originalUri
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playable URI: ${e.message}", e)
            return null
        }
    }

    private fun createLocalCopy(context: Context, uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(context, uri) ?: "audio_${System.currentTimeMillis()}.mp3"

            val audioDir = File(context.filesDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val outputFile = File(audioDir, fileName)

            inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local copy: ${e.message}", e)
            return null
        }
    }


    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }

        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex("_display_name")
                        if (displayNameIndex != -1) {
                            return cursor.getString(displayNameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file name from URI: ${e.message}", e)
            }
        }

        return "audio_${System.currentTimeMillis()}.mp3"
    }

    private fun findExistingCopy(context: Context, fileName: String?): File? {
        if (fileName.isNullOrEmpty()) return null

        val audioDir = File(context.filesDir, "audio")
        val file = File(audioDir, fileName)

        return if (file.exists() && file.canRead()) file else null
    }

    fun clearAudioCache(context: Context) {
        try {
            val audioDir = File(context.filesDir, "audio")
            if (audioDir.exists()) {
                audioDir.listFiles()?.forEach { file ->
                    val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                    if (file.lastModified() < oneDayAgo) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing audio cache: ${e.message}", e)
        }
    }
}
