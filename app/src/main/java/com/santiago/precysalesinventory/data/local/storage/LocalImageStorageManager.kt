package com.santiago.precysalesinventory.data.local.storage

import android.content.Context
import android.net.Uri
import com.santiago.precysalesinventory.domain.repository.ImageStorageManager
import java.io.File
import java.io.FileOutputStream

class LocalImageStorageManager(private val context: Context) : ImageStorageManager {
    override fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
