package com.stencilla.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageFileUtil {

    fun createCaptureUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera_captures").also { it.mkdirs() }
        val file = File(cacheDir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun copyToCache(context: Context, uri: Uri): File {
        val cacheDir = File(context.cacheDir, "camera_captures").also { it.mkdirs() }
        val dest = File(cacheDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        } ?: error("Cannot open URI: $uri")
        return dest
    }

    fun copyToInternalStorage(context: Context, uri: Uri): File {
        val dir = File(context.filesDir, "wardrobe").also { it.mkdirs() }
        val dest = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        } ?: error("Cannot open URI: $uri")
        return dest
    }

    fun fileToMultipart(file: File): MultipartBody.Part {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, requestBody)
    }
}
