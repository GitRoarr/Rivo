package com.rivo.app.utils
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MediaAccessHelper(private val activity: FragmentActivity) {

    private val TAG = "MediaAccessHelper"

    private val openDocument = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleMediaUri(it) }
    }

    private val openMultipleDocuments = activity.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris?.forEach { handleMediaUri(it) }
    }

    private val getContent = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleMediaUri(it) }
    }

    private var onMediaSelectedCallback: ((Uri) -> Unit)? = null

    fun pickAudioFile(onAudioSelected: (Uri) -> Unit) {
        onMediaSelectedCallback = onAudioSelected

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getContent.launch("audio/*")
        } else {
            openDocument.launch(arrayOf("audio/*"))
        }
    }


    private fun handleMediaUri(uri: Uri) {
        try {
            if (uri.scheme == "content") {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    activity.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    Log.d(TAG, "Successfully obtained persistent permission for URI: $uri")
                } catch (e: SecurityException) {
                    Log.e(TAG, "Failed to take persistent permission: ${e.message}")
                }
            }

            if (isUriAccessible(activity, uri)) {
                onMediaSelectedCallback?.invoke(uri)
            } else {
                Log.e(TAG, "URI is not accessible, copying to app storage: $uri")
                activity.lifecycleScope.launch {
                    val localUri = copyMediaToAppStorage(uri)
                    localUri?.let {
                        onMediaSelectedCallback?.invoke(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling media URI: ${e.message}", e)
            activity.lifecycleScope.launch {
                val localUri = copyMediaToAppStorage(uri)
                localUri?.let {
                    onMediaSelectedCallback?.invoke(it)
                }
            }
        }
    }

    private suspend fun copyMediaToAppStorage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = activity.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = getFileNameFromUri(activity, uri) ?: "media_${System.currentTimeMillis()}"
            val fileExtension = getFileExtension(activity, uri)

            val mediaDir = File(activity.filesDir, "media")
            if (!mediaDir.exists()) {
                mediaDir.mkdirs()
            }

            val outputFile = File(mediaDir, "$fileName$fileExtension")

            inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Successfully copied file to: ${outputFile.absolutePath}")
            return@withContext Uri.fromFile(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error copying media file: ${e.message}", e)
            return@withContext null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                        // Remove extension from filename
                        fileName = fileName?.substringBeforeLast('.')
                    }
                }
            }
        } else if (uri.scheme == "file") {
            fileName = uri.lastPathSegment?.substringBeforeLast('.')
        }

        return fileName ?: "media_${System.currentTimeMillis()}"
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        var extension = ""

        if (uri.scheme == "content") {
            val mimeType = context.contentResolver.getType(uri)
            extension = when {
                mimeType?.contains("image") == true -> ".jpg"
                mimeType?.contains("audio") == true -> ".mp3"
                mimeType?.contains("video") == true -> ".mp4"
                else -> {
                    val displayName = getFileNameFromUri(context, uri)
                    displayName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                }
            }
        } else if (uri.scheme == "file") {
            extension = uri.lastPathSegment?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
        }

        return extension
    }

    companion object {
        fun isUriAccessible(context: Context, uri: Uri): Boolean {
            return try {
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: "")
                    file.exists() && file.canRead()
                } else {
                    context.contentResolver.getType(uri) != null
                }
            } catch (e: Exception) {
                Log.e("MediaAccessHelper", "Cannot access URI: $uri", e)
                false
            }
        }


        fun hasUriPermission(context: Context, uri: Uri): Boolean {
            if (uri.scheme == "file") {
                return true
            }

            if (uri.scheme == "content") {
                try {
                    val persistedUriPermissions = context.contentResolver.persistedUriPermissions
                    val hasPersistedPermission = persistedUriPermissions.any {
                        it.uri.toString() == uri.toString() && it.isReadPermission
                    }

                    if (hasPersistedPermission) {
                        return true
                    }

                    context.contentResolver.openInputStream(uri)?.close()
                    return true
                } catch (e: SecurityException) {
                    Log.e("MediaAccessHelper", "Permission not valid for URI: $uri", e)
                    return false
                } catch (e: IOException) {
                    Log.e("MediaAccessHelper", "IO error accessing URI: $uri", e)
                    return false
                }
            }

            return false
        }


        fun resolveUri(context: Context, uri: Uri): Uri {
            if (uri.scheme == "file") return uri

            if (uri.scheme == "content") {
                if (hasUriPermission(context, uri)) {
                    return uri
                }

                val localFile = getLocalFileFromUri(context, uri)
                if (localFile != null && localFile.exists()) {
                    return Uri.fromFile(localFile)
                }

                return try {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return uri
                    val fileName = System.currentTimeMillis().toString() + ".mp3"
                    val outputFile = File(context.filesDir, "media/$fileName")
                    outputFile.parentFile?.mkdirs()
                    inputStream.use { input ->
                        outputFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    Uri.fromFile(outputFile)
                } catch (e: Exception) {
                    Log.e("MediaAccessHelper", "Failed to copy inaccessible URI fallback", e)
                    uri
                }
            }
            return uri
        }


        private fun getLocalFileFromUri(context: Context, uri: Uri): File? {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: return null
            val mediaDir = File(context.filesDir, "media")
            val potentialFile = File(mediaDir, fileName)

            if (potentialFile.exists()) {
                return potentialFile
            }

            return null
        }


    }
}