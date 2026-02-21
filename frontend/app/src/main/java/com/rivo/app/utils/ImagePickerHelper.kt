package com.rivo.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


object ImagePickerHelper {

    fun saveImageToInternalStorageAsync(
        context: Context,
        uri: Uri,
        fileName: String? = null,
        callback: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = saveImageToInternalStorage(context, uri, fileName)
            callback(result)
        }
    }


    suspend fun saveImageToInternalStorage(
        context: Context,
        uri: Uri,
        fileName: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val actualFileName = fileName ?: "img_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, actualFileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()

                    context.contentResolver.openInputStream(uri)?.use { newInputStream ->
                        val sampleSize = calculateInSampleSize(options, 1024, 1024)
                        val finalOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                        }

                        val bitmap = BitmapFactory.decodeStream(newInputStream, null, finalOptions)

                        if (bitmap != null) {
                            FileOutputStream(file).use { outputStream ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                                outputStream.flush()
                            }
                            bitmap.recycle()
                            return@withContext file.absolutePath
                        }
                    }
                }
                null
            } catch (e: IOException) {
                Log.e("ImagePickerHelper", "Error saving image: ${e.message}", e)
                null
            } catch (e: SecurityException) {
                Log.e("ImagePickerHelper", "Security exception: ${e.message}", e)
                null
            }
        }
    }


    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
