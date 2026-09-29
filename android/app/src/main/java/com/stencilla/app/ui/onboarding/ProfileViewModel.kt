package com.stencilla.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.remote.dto.ProfileUpdateRequest
import com.stencilla.app.data.repository.AuthRepository
import com.stencilla.app.data.repository.ProfileRepository
import com.stencilla.app.util.ApiErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val fullName: String = "",
    val age: String = "",
    val gender: String? = null,
    val lifestyle: String? = null,
    val heightCm: String = "",
    val bodyType: String? = null,
    val skinTone: String? = null,
    val styleGoal: String? = null,
    val preferredColors: String = "",
    val preferredFits: String? = null,
    val preferredSilhouettes: String? = null,
    val comfortPriority: String? = null,
    val formalityPreference: String? = null,
    val modestyPreference: String? = null,
    val styleInspirations: String = "",
    val confidenceSignals: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile().getOrNull() ?: return@launch
                _uiState.update {
                    it.copy(
                        fullName = profile.fullName.orEmpty(),
                        age = profile.age?.toString().orEmpty(),
                        gender = profile.gender,
                        lifestyle = profile.lifestyle,
                        heightCm = profile.heightCm?.toString().orEmpty(),
                        bodyType = profile.bodyType,
                        skinTone = profile.skinTone,
                        styleGoal = profile.styleGoal,
                        preferredColors = profile.preferredColors.orEmpty(), preferredFits = profile.preferredFits,
                        preferredSilhouettes = profile.preferredSilhouettes, comfortPriority = profile.comfortPriority,
                        formalityPreference = profile.formalityPreference, modestyPreference = profile.modestyPreference,
                        styleInspirations = profile.styleInspirations.orEmpty(), confidenceSignals = profile.confidenceSignals.orEmpty(),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ApiErrorParser.messageFor(e)) }
            }
        }
    }

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v) }
    fun onAgeChange(v: String) = _uiState.update { it.copy(age = v.filter { c -> c.isDigit() }) }
    fun onHeightChange(v: String) = _uiState.update { it.copy(heightCm = v.filter { c -> c.isDigit() }) }
    fun onGenderSelect(v: String) = _uiState.update { it.copy(gender = v) }
    fun onLifestyleSelect(v: String) = _uiState.update { it.copy(lifestyle = v) }
    fun onBodyTypeSelect(v: String) = _uiState.update { it.copy(bodyType = v) }
    fun onSkinToneSelect(v: String) = _uiState.update { it.copy(skinTone = v) }
    fun onStyleGoalSelect(v: String) = _uiState.update { it.copy(styleGoal = v) }
    fun onPreferredColorsChange(v: String) = _uiState.update { it.copy(preferredColors = v) }
    fun onPreferredFitsSelect(v: String) = _uiState.update { it.copy(preferredFits = v) }
    fun onPreferredSilhouettesSelect(v: String) = _uiState.update { it.copy(preferredSilhouettes = v) }
    fun onComfortPrioritySelect(v: String) = _uiState.update { it.copy(comfortPriority = v) }
    fun onFormalityPreferenceSelect(v: String) = _uiState.update { it.copy(formalityPreference = v) }
    fun onModestyPreferenceSelect(v: String) = _uiState.update { it.copy(modestyPreference = v) }
    fun onStyleInspirationsChange(v: String) = _uiState.update { it.copy(styleInspirations = v) }
    fun onConfidenceSignalsChange(v: String) = _uiState.update { it.copy(confidenceSignals = v) }

    fun save() {
        val s = _uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                profileRepository.saveProfile(
                    ProfileUpdateRequest(
                        fullName = s.fullName.ifBlank { null },
                        age = s.age.toIntOrNull(),
                        gender = s.gender,
                        lifestyle = s.lifestyle,
                        heightCm = s.heightCm.toIntOrNull(),
                        bodyType = s.bodyType,
                        skinTone = s.skinTone,
                        styleGoal = s.styleGoal,
                        preferredColors = s.preferredColors.trim().takeIf { it.isNotEmpty() }, preferredFits = s.preferredFits,
                        preferredSilhouettes = s.preferredSilhouettes, comfortPriority = s.comfortPriority,
                        formalityPreference = s.formalityPreference, modestyPreference = s.modestyPreference,
                        styleInspirations = s.styleInspirations.trim().takeIf { it.isNotEmpty() }, confidenceSignals = s.confidenceSignals.trim().takeIf { it.isNotEmpty() },
                    ),
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = ApiErrorParser.messageFor(e)) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
