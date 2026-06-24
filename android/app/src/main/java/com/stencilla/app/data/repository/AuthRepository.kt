package com.stencilla.app.data.repository

import com.stencilla.app.data.local.AuthTokenStore
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.LoginRequest
import com.stencilla.app.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: AuthTokenStore,
) {
    val isLoggedIn: Flow<Boolean> = tokenStore.tokenFlow.map { it != null }

    suspend fun register(email: String, password: String, fullName: String?) {
        val response = api.register(RegisterRequest(email = email, password = password, fullName = fullName))
        tokenStore.saveToken(response.accessToken)
    }

    suspend fun login(email: String, password: String) {
        val response = api.login(LoginRequest(email = email, password = password))
        tokenStore.saveToken(response.accessToken)
    }

    suspend fun logout() {
        tokenStore.clearToken()
    }
}
