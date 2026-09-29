package com.stencilla.app.ui.closet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.remote.dto.CategoryCountDto
import com.stencilla.app.data.remote.dto.ColorEntryDto
import com.stencilla.app.data.remote.dto.SmartCategoryDto
import com.stencilla.app.data.repository.WardrobeAnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClosetUiState(
    val allItems: List<ClothingItemEntity> = emptyList(),
    val filteredItems: List<ClothingItemEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: String = "All",
    val totalCount: Int = 0,
    val outfitCount: Int = 0,
    val utilizationPct: Int = 0,
    val topColors: List<ColorEntryDto> = emptyList(),
    val categoryBreakdown: List<CategoryCountDto> = emptyList(),
    val smartCategories: List<SmartCategoryDto> = emptyList(),
    val aiSuggestion: String? = null,
    val isLoadingItems: Boolean = false,
    val snackMessage: String? = null,
)

@HiltViewModel
class ClosetViewModel @Inject constructor(
    private val clothingDao: ClothingItemDao,
    private val analyticsRepo: WardrobeAnalyticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClosetUiState())
    val uiState: StateFlow<ClosetUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            clothingDao.observeAll().collect { items ->
                _uiState.update { it.copy(allItems = items, totalCount = items.size) }
                applyFilterAndSearch()
            }
        }
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            try {
                val analytics = analyticsRepo.getAnalytics()
                _uiState.update {
                    it.copy(
                        outfitCount = analytics.outfitCount,
                        utilizationPct = analytics.utilizationPct,
                        topColors = analytics.topColors,
                        categoryBreakdown = analytics.categoryBreakdown,
                        smartCategories = analytics.smartCategories,
                        aiSuggestion = analytics.aiSuggestion.takeIf { s -> s.isNotBlank() },
                    )
                }
            } catch (_: Exception) { /* offline — keep local data */ }
        }
    }

    fun onSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            applyFilterAndSearch()
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            try {
                val matchingIds = analyticsRepo.search(query).toSet()
                val filtered = _uiState.value.allItems.filter { it.id in matchingIds }
                _uiState.update { it.copy(filteredItems = filtered) }
            } catch (_: Exception) {
                // Fallback to local text match
                val q = query.lowercase()
                val local = _uiState.value.allItems.filter { item ->
                    listOfNotNull(item.category, item.subcategory, item.colorPrimary,
                        item.colorSecondary, item.brand, item.pattern, item.formality)
                        .any { it.lowercase().contains(q) }
                }
                _uiState.update { it.copy(filteredItems = local) }
            }
        }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
        viewModelScope.launch {
            if (filter == "All") {
                applyFilterAndSearch()
            } else {
                try {
                    val matchingIds = analyticsRepo.filter(category = filter.lowercase()).toSet()
                    val filtered = _uiState.value.allItems.filter { it.id in matchingIds }
                    _uiState.update { it.copy(filteredItems = filtered) }
                } catch (_: Exception) {
                    applyFilterAndSearch()
                }
            }
        }
    }

    fun selectSmartCategory(label: String) {
        viewModelScope.launch {
            val smartCat = _uiState.value.smartCategories.firstOrNull { it.label == label }
            if (smartCat != null) {
                val ids = smartCat.itemIds.toSet()
                val filtered = _uiState.value.allItems.filter { it.id in ids }
                _uiState.update { it.copy(filteredItems = filtered, activeFilter = label) }
            }
        }
    }

    private fun applyFilterAndSearch() {
        val state = _uiState.value
        var items = state.allItems
        if (state.activeFilter != "All") {
            val filter = state.activeFilter.lowercase()
            items = items.filter { item ->
                listOfNotNull(item.category, item.subcategory)
                    .any { it.lowercase().contains(filter) }
            }
        }
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase()
            items = items.filter { item ->
                listOfNotNull(item.category, item.subcategory, item.colorPrimary,
                    item.colorSecondary, item.brand, item.pattern, item.formality)
                    .any { it.lowercase().contains(q) }
            }
        }
        _uiState.update { it.copy(filteredItems = items) }
    }

    fun clearSnack() = _uiState.update { it.copy(snackMessage = null) }
}
