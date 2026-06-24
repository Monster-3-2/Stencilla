package com.stencilla.app.ui.closet

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.repository.WardrobeRepository
import com.stencilla.app.util.ApiErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddItemUiState(
    val selectedImageUri: Uri? = null,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val uploaded: Boolean = false,
)

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedImageUri = null) }
    }

    fun upload(context: Context) {
        val uri = _uiState.value.selectedImageUri ?: return
        _uiState.update { it.copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                wardrobeRepository.addItem(context, uri)
                _uiState.update { it.copy(isUploading = false, uploaded = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, errorMessage = ApiErrorParser.messageFor(e)) }
            }
        }
    }
}
