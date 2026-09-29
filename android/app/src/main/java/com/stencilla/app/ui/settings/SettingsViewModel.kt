package com.stencilla.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.remote.dto.SettingsDto
import com.stencilla.app.data.repository.AuthRepository
import com.stencilla.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val settings: StateFlow<SettingsDto> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsDto())

    init {
        viewModelScope.launch { settingsRepo.sync() }
    }

    fun update(dto: SettingsDto) {
        viewModelScope.launch { settingsRepo.updateSettings(dto) }
    }

    fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}
