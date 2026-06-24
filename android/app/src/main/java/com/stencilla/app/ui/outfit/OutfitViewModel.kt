package com.stencilla.app.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.remote.dto.OutfitResponse
import com.stencilla.app.data.repository.OutfitRepository
import com.stencilla.app.data.repository.WardrobeRepository
import com.stencilla.app.util.ApiErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OutfitUiState(
    val occasion: String = "casual",
    val anchorItemId: String? = null,
    val notes: String = "",
    val isGenerating: Boolean = false,
    val result: OutfitResponse? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class OutfitViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val wardrobeRepository: WardrobeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitUiState())
    val uiState: StateFlow<OutfitUiState> = _uiState.asStateFlow()

    /** Live local closet, used both for the anchor-item picker and as the payload sent
     * with every outfit request (the backend has no wardrobe state of its own). */
    val wardrobeItems: StateFlow<List<ClothingItemEntity>> = wardrobeRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onOccasionSelect(value: String) = _uiState.update { it.copy(occasion = value) }
    fun onAnchorSelect(itemId: String?) = _uiState.update { it.copy(anchorItemId = itemId) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun generateOutfit() {
        val state = _uiState.value
        val wardrobe = wardrobeItems.value
        if (wardrobe.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add some clothing photos first.") }
            return
        }
        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = outfitRepository.suggestOutfit(
                    occasion = state.occasion,
                    wardrobe = wardrobe,
                    anchorItemId = state.anchorItemId,
                    notes = state.notes.ifBlank { null },
                )
                _uiState.update { it.copy(isGenerating = false, result = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, errorMessage = ApiErrorParser.messageFor(e)) }
            }
        }
    }

    /** Resolves the chosen item ids from the AI response back to full local entities for display. */
    fun resolveResultItems(): List<ClothingItemEntity> {
        val ids = _uiState.value.result?.itemIds ?: return emptyList()
        val byId = wardrobeItems.value.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    fun dismissResult() = _uiState.update { it.copy(result = null) }
}
