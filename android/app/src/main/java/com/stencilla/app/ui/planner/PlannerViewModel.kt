package com.stencilla.app.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.PlannerEventEntity
import com.stencilla.app.data.repository.PlannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class PlannerUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDate: String? = null,
    val calendarEvents: List<PlannerEventEntity> = emptyList(),
    val upcomingEvents: List<PlannerEventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val outfitPlanningEventId: Int? = null,
    val snackMessage: String? = null,
)

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val repo: PlannerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collect { all ->
                val upcoming = repo.getUpcoming(7)
                _uiState.update { it.copy(upcomingEvents = upcoming) }
                loadCalendar()
            }
        }
        viewModelScope.launch { repo.sync() }
    }

    fun selectDate(dateStr: String) {
        _uiState.update { it.copy(selectedDate = dateStr) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
        loadCalendar()
    }

    fun previousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
        loadCalendar()
    }

    private fun loadCalendar() {
        viewModelScope.launch {
            val m = _uiState.value.selectedMonth
            val events = repo.getByMonth(m.year, m.monthValue)
            _uiState.update { it.copy(calendarEvents = events) }
        }
    }

    fun createEvent(
        title: String, date: String, time: String?,
        occasion: String?, dressCode: String?, notes: String?,
        isTrip: Boolean, tripEndDate: String?,
    ) {
        viewModelScope.launch {
            repo.create(title, date, time, occasion, dressCode, notes, isTrip, tripEndDate)
            _uiState.update { it.copy(upcomingEvents = repo.getUpcoming()) }
            loadCalendar()
        }
    }

    fun deleteEvent(event: PlannerEventEntity) {
        viewModelScope.launch {
            repo.delete(event)
            _uiState.update { s -> s.copy(upcomingEvents = s.upcomingEvents.filter { it.id != event.id }) }
            loadCalendar()
        }
    }

    fun planOutfitForEvent(eventId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(outfitPlanningEventId = eventId) }
            val result = repo.planOutfitForEvent(eventId)
            val msg = if (result != null) "Outfit planned for event!" else "Could not plan outfit — check connection."
            _uiState.update { it.copy(outfitPlanningEventId = null, snackMessage = msg) }
            loadCalendar()
        }
    }

    fun clearSnack() = _uiState.update { it.copy(snackMessage = null) }
}
