package com.stencilla.app.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.local.db.OutfitEntity
import com.stencilla.app.data.remote.dto.OutfitResponse
import com.stencilla.app.data.repository.OutfitRepository
import com.stencilla.app.data.repository.WardrobeRepository
import com.stencilla.app.data.repository.ContextRepository
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
    val dressCode: String = "unspecified",
    val anchorItemId: String? = null,
    val notes: String = "",
    val isGenerating: Boolean = false,
    val result: OutfitResponse? = null,
    val errorMessage: String? = null,
    val weatherTemp: Double = 22.0,
    val weatherCondition: String = "Sunny",
    val calendarEvent: String = "None",
    val contextMessage: String? = null,
    val latestOutfitId: String? = null,
    val feedbackOutcome: String? = null,
    val feedbackRating: Int? = null,
    val feedbackReason: String = "",
    val feedbackSaved: Boolean = false,
)

@HiltViewModel
class OutfitViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val wardrobeRepository: WardrobeRepository,
    private val contextRepository: ContextRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitUiState())
    val uiState: StateFlow<OutfitUiState> = _uiState.asStateFlow()

    /** Live local closet, used both for the anchor-item picker and as the payload sent
     * with every outfit request (the backend has no wardrobe state of its own). */
    val wardrobeItems: StateFlow<List<ClothingItemEntity>> = wardrobeRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<OutfitEntity>> = outfitRepository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { refreshDeviceContext() }

    fun refreshDeviceContext() {
        viewModelScope.launch {
            val device = contextRepository.load()
            _uiState.update { state ->
                state.copy(
                    weatherTemp = device.weather?.temperature ?: state.weatherTemp,
                    weatherCondition = device.weather?.condition ?: state.weatherCondition,
                    calendarEvent = device.calendarEvent ?: state.calendarEvent,
                    contextMessage = if (device.weather == null && device.calendarEvent == null)
                        "Allow location and calendar access to personalize this suggestion." else null,
                )
            }
        }
    }

    fun onOccasionSelect(value: String) = _uiState.update { it.copy(occasion = value) }
    fun onDressCodeSelect(value: String) = _uiState.update { it.copy(dressCode = value) }
    fun onAnchorSelect(itemId: String?) = _uiState.update { it.copy(anchorItemId = itemId) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onWeatherTempChange(value: Double) = _uiState.update { it.copy(weatherTemp = value) }
    fun onWeatherConditionSelect(value: String) = _uiState.update { it.copy(weatherCondition = value) }
    fun onCalendarEventChange(value: String) = _uiState.update { it.copy(calendarEvent = value) }
    fun onFeedbackOutcome(value: String) = _uiState.update { it.copy(feedbackOutcome = value, feedbackSaved = false) }
    fun onFeedbackRating(value: Int) = _uiState.update { it.copy(feedbackRating = value, feedbackSaved = false) }
    fun onFeedbackReason(value: String) = _uiState.update { it.copy(feedbackReason = value, feedbackSaved = false) }

    fun saveFeedback() {
        val state = _uiState.value
        val outfitId = state.latestOutfitId ?: return
        val outcome = state.feedbackOutcome ?: return
        viewModelScope.launch {
            outfitRepository.saveFeedback(outfitId, outcome, state.feedbackRating, state.feedbackReason)
            _uiState.update { it.copy(feedbackSaved = true) }
        }
    }

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
                    dressCode = state.dressCode.takeIf { it != "unspecified" },
                    wardrobe = wardrobe,
                    anchorItemId = state.anchorItemId,
                    notes = state.notes.ifBlank { null },
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition.ifBlank { null },
                    calendarEvent = state.calendarEvent.takeIf { it != "None" && it.isNotBlank() },
                    preferenceSummary = outfitRepository.preferenceSummary(),
                )
                val savedOutfit = outfitRepository.saveOutfit(result)
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        result = result,
                        latestOutfitId = savedOutfit.id,
                        feedbackOutcome = null,
                        feedbackRating = null,
                        feedbackReason = "",
                        feedbackSaved = false,
                    )
                }
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
