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
    val brand: String = "",
    val size: String = "",
    val condition: String = "good",
    val availability: String = "available",
    val laundryState: String = "clean",
    val repairNote: String = "",
    val purchasePrice: String = "",
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

    fun onBrandChange(value: String) = _uiState.update { it.copy(brand = value) }
    fun onSizeChange(value: String) = _uiState.update { it.copy(size = value) }
    fun onConditionChange(value: String) = _uiState.update { it.copy(condition = value) }
    fun onAvailabilityChange(value: String) = _uiState.update { it.copy(availability = value) }
    fun onLaundryStateChange(value: String) = _uiState.update { it.copy(laundryState = value) }
    fun onRepairNoteChange(value: String) = _uiState.update { it.copy(repairNote = value) }
    fun onPurchasePriceChange(value: String) = _uiState.update { it.copy(purchasePrice = value) }

    fun upload(context: Context) {
        val uri = _uiState.value.selectedImageUri ?: return
        _uiState.update { it.copy(isUploading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val current = _uiState.value
                wardrobeRepository.addItem(
                    context, uri, current.brand, current.size, current.condition,
                    current.availability, current.laundryState, current.repairNote,
                    current.purchasePrice.toDoubleOrNull()?.takeIf { it >= 0 }?.times(100)?.toInt(),
                )
                _uiState.update { it.copy(isUploading = false, uploaded = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, errorMessage = ApiErrorParser.messageFor(e)) }
            } finally {
                // Camera captures live in cache until explicitly removed. Never remove a
                // user-selected gallery URI, which may be shared external storage.
                if (uri.authority == "${context.packageName}.fileprovider") {
                    runCatching { context.contentResolver.delete(uri, null, null) }
                }
            }
        }
    }
}
