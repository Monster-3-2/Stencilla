package com.stencilla.app.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

object ImageFileUtil {

    /**
     * Copies the content at [uri] into this app's private internal storage (filesDir), where
     * it persists for as long as the app is installed and is never visible to other apps or
     * the user's gallery. This is the permanent home for a saved wardrobe photo.
     */
    fun copyToInternalStorage(context: Context, uri: Uri): File {
        val closetDir = File(context.filesDir, "closet_photos").apply { mkdirs() }
        val outFile = File(closetDir, "${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open image stream for $uri")

        return outFile
    }

    /** Wraps an already-saved local file as a multipart part for the one-shot tagging upload. */
    fun fileToMultipart(file: File, partName: String = "image"): MultipartBody.Part {
        val requestBody = file.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }

    /** Creates an empty jpg file in cache and returns a content:// Uri for it, for ACTION_IMAGE_CAPTURE output. */
    fun createCaptureUri(context: Context): Uri {
        val captureDir = File(context.cacheDir, "captured_images").apply { mkdirs() }
        val file = File(captureDir, "capture_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
