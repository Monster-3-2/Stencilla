package com.stencilla.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val email: String? = null,
    val fullName: String? = null,
    val styleGoal: String? = null,
    val styleInspirations: String? = null,
    val bodyType: String? = null,
    val heightCm: Int? = null,
    val preferredColors: String? = null,
    val preferredFits: String? = null,
    val formalityPreference: String? = null,
    val comfortPriority: String? = null,
    val lifestyle: String? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val profile = api.getProfile()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        email = profile.email,
                        fullName = profile.fullName,
                        styleGoal = profile.styleGoal,
                        styleInspirations = profile.styleInspirations,
                        bodyType = profile.bodyType,
                        heightCm = profile.heightCm,
                        preferredColors = profile.preferredColors,
                        preferredFits = profile.preferredFits,
                        formalityPreference = profile.formalityPreference,
                        comfortPriority = profile.comfortPriority,
                        lifestyle = profile.lifestyle,
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun startEditing() {
        // Navigation to ProfileSetupScreen handled by nav graph
    }
}
