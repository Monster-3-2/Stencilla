package com.stencilla.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.remote.dto.OutfitFactorDto
import com.stencilla.app.data.repository.ContextRepository
import com.stencilla.app.data.repository.OutfitRepository
import com.stencilla.app.data.repository.PlannerRepository
import com.stencilla.app.data.local.db.ClothingItemDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TodayUiState(
    val greeting: String = "Good morning",
    val weatherTemp: Double? = null,
    val weatherCondition: String? = null,
    val calendarEvent: String? = null,
    val upcomingEventContext: String? = null,
    val isGenerating: Boolean = false,
    val outfitReasoning: String? = null,
    val outfitFactors: List<OutfitFactorDto> = emptyList(),
    val stylistReply: String? = null,
    val error: String? = null,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val contextRepo: ContextRepository,
    private val outfitRepo: OutfitRepository,
    private val plannerRepo: PlannerRepository,
    private val clothingDao: ClothingItemDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(greeting = greeting()))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        refreshDeviceContext()
        viewModelScope.launch {
            val ctx = plannerRepo.upcomingEventContext()
            _uiState.update { it.copy(upcomingEventContext = ctx) }
        }
    }

    fun refreshDeviceContext() {
        viewModelScope.launch {
            try {
                val ctx = contextRepo.load()
                _uiState.update {
                    it.copy(
                        weatherTemp = ctx.weather?.temperature,
                        weatherCondition = ctx.weather?.condition,
                        calendarEvent = ctx.calendarEvent,
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun refreshOutfit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                val wardrobe = clothingDao.getAll().filter { it.aiTagged }
                if (wardrobe.isEmpty()) {
                    _uiState.update { it.copy(isGenerating = false, outfitReasoning = "Add some clothes to your closet first!") }
                    return@launch
                }
                val state = _uiState.value
                val calEvent = listOfNotNull(state.calendarEvent, state.upcomingEventContext).firstOrNull()
                val pref = outfitRepo.preferenceSummary()
                val response = outfitRepo.suggestOutfit(
                    occasion = "casual",
                    wardrobe = wardrobe,
                    anchorItemId = null,
                    notes = null,
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition,
                    calendarEvent = calEvent,
                    preferenceSummary = pref,
                )
                outfitRepo.saveOutfit(response)
                _uiState.update {
                    it.copy(isGenerating = false, outfitReasoning = response.reasoning, outfitFactors = response.factors)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun askStylist(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, stylistReply = null) }
            try {
                val wardrobe = clothingDao.getAll().filter { it.aiTagged }
                val state = _uiState.value
                val response = outfitRepo.suggestOutfit(
                    occasion = "casual",
                    wardrobe = wardrobe,
                    anchorItemId = null,
                    notes = query,
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition,
                    calendarEvent = state.calendarEvent,
                    preferenceSummary = null,
                )
                _uiState.update {
                    it.copy(isGenerating = false, stylistReply = response.reasoning, outfitFactors = response.factors)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, stylistReply = "Couldn't reach the stylist. Check your connection.") }
            }
        }
    }

    private fun greeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            else      -> "Good evening"
        }
    }
}
