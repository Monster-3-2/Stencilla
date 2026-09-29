package com.stencilla.app.ui.verifier

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.OutfitVerificationResponse
import com.stencilla.app.util.ApiErrorParser
import com.stencilla.app.util.ImageFileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

data class OutfitVerifierUiState(
    val selectedUri: Uri? = null,
    val selectedMimeType: String = "image/jpeg",
    val consentGiven: Boolean = false,
    val keepOnDevice: Boolean = false,
    val isAnalyzing: Boolean = false,
    val result: OutfitVerificationResponse? = null,
    val retainedFilePath: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class OutfitVerifierViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutfitVerifierUiState())
    val uiState = _uiState.asStateFlow()

    fun selectImage(context: Context, uri: Uri, mimeType: String? = null) {
        cleanupCapture(context, _uiState.value.selectedUri)
        deleteRetainedImage()
        _uiState.update {
            it.copy(
                selectedUri = uri,
                selectedMimeType = mimeType ?: "image/jpeg",
                consentGiven = false,
                result = null,
                errorMessage = null,
            )
        }
    }
    fun setConsent(value: Boolean) = _uiState.update { it.copy(consentGiven = value) }
    fun setKeepOnDevice(value: Boolean) = _uiState.update { it.copy(keepOnDevice = value) }

    fun analyze(context: Context) {
        val state = _uiState.value
        val uri = state.selectedUri ?: return
        if (!state.consentGiven || state.isAnalyzing) return
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            var tempFile: File? = null
            try {
                tempFile = ImageFileUtil.copyToCache(context, uri)
                val mimeType = state.selectedMimeType
                val result = api.analyzeOutfit(
                    MultipartBody.Part.createFormData(
                        "image", tempFile.name, tempFile.asRequestBody(mimeType.toMediaType()),
                    ),
                    "true".toRequestBody("text/plain".toMediaType()),
                    "false".toRequestBody("text/plain".toMediaType()),
                )
                val retainedPath = if (state.keepOnDevice) tempFile.absolutePath else null
                _uiState.update { it.copy(isAnalyzing = false, result = result, retainedFilePath = retainedPath) }
                if (!state.keepOnDevice) tempFile.delete()
                cleanupCapture(context, uri)
            } catch (error: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, errorMessage = ApiErrorParser.messageFor(error)) }
                tempFile?.delete()
            }
        }
    }

    fun deleteRetainedImage() {
        _uiState.value.retainedFilePath?.let { path ->
            File(path).takeIf { it.isFile }?.delete()
        }
        _uiState.update { it.copy(retainedFilePath = null, keepOnDevice = false) }
    }

    fun clearSelection(context: Context) {
        cleanupCapture(context, _uiState.value.selectedUri)
        deleteRetainedImage()
        _uiState.update { it.copy(selectedUri = null, result = null, errorMessage = null) }
    }

    private fun cleanupCapture(context: Context, uri: Uri?) {
        if (uri?.authority == null) return
        // Camera captures are created in the app cache through our FileProvider. Never remove
        // arbitrary picker content owned by another app.
        if (uri.authority == "${context.packageName}.fileprovider") {
            context.contentResolver.delete(uri, null, null)
        }
    }

    override fun onCleared() {
        _uiState.value.retainedFilePath?.let { File(it).delete() }
        super.onCleared()
    }
}
