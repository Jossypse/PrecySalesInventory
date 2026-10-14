package com.santiago.precysalesinventory.domain.repository

import android.net.Uri

interface ImageStorageManager {
    fun saveImageToInternalStorage(uri: Uri): String?
}
