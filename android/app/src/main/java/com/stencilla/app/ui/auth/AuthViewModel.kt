package com.stencilla.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stencilla.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") } },
            )
        }
    }

    fun register(email: String, password: String, fullName: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(email, password, fullName)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") } },
            )
        }
    }
}
