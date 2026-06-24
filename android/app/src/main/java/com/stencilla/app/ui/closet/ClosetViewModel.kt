package com.stencilla.app.ui.closet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.repository.WardrobeRepository
import com.stencilla.app.util.ApiErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val CLOSET_TABS = listOf(
    "All", "Shirts", "T-Shirts", "Jackets", "Coats",
    "Jeans", "Trousers", "Shorts", "Shoes", "Accessories",
)

private fun tabToCategory(tab: String): String? = when (tab) {
    "All" -> null
    "Shirts" -> "shirt"
    "T-Shirts" -> "tshirt"
    "Jackets" -> "jacket"
    "Coats" -> "coat"
    "Jeans" -> "jeans"
    "Trousers" -> "trousers"
    "Shorts" -> "shorts"
    "Shoes" -> "shoes"
    "Accessories" -> "accessory"
    else -> null
}

data class ClosetUiState(
    val selectedTab: String = "All",
    val pendingClarificationItem: ClothingItemEntity? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ClosetViewModel @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClosetUiState())
    val uiState: StateFlow<ClosetUiState> = _uiState.asStateFlow()

    private val _allItems: StateFlow<List<ClothingItemEntity>> = wardrobeRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<ClothingItemEntity>> = _uiState
        .map { state ->
            val category = tabToCategory(state.selectedTab)
            if (category == null) _allItems.value
            else _allItems.value.filter { it.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Watch for items needing clarification and surface the dialog
        viewModelScope.launch {
            _allItems.collect { list ->
                val pending = list.firstOrNull { it.needsClarification && it.clarificationQuestion != null }
                _uiState.update { it.copy(pendingClarificationItem = pending) }
                // Re-filter when allItems changes
                val category = tabToCategory(_uiState.value.selectedTab)
                val filtered = if (category == null) list else list.filter { item -> item.category == category }
                // StateFlow will recompute via the map above on next collect
            }
        }
    }

    fun selectTab(tab: String) = _uiState.update { it.copy(selectedTab = tab) }

    fun deleteItem(item: ClothingItemEntity) {
        viewModelScope.launch {
            try {
                wardrobeRepository.deleteItem(item)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = ApiErrorParser.messageFor(e)) }
            }
        }
    }

    fun submitClarification(id: String, material: String?, fit: String?) {
        viewModelScope.launch {
            wardrobeRepository.saveClarification(id, material, fit)
            _uiState.update { it.copy(pendingClarificationItem = null) }
        }
    }

    fun dismissClarification() = _uiState.update { it.copy(pendingClarificationItem = null) }
}
