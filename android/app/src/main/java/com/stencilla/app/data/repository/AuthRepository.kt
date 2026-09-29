package com.stencilla.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.LoginRequest
import com.stencilla.app.data.remote.dto.RegisterRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authStore by preferencesDataStore(name = "stencilla_auth")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
) {
    private val KEY_TOKEN = stringPreferencesKey("access_token")

    val tokenFlow: Flow<String?> = context.authStore.data.map { it[KEY_TOKEN] }

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email = email, password = password))
        context.authStore.edit { it[KEY_TOKEN] = response.accessToken }
    }

    suspend fun register(email: String, password: String, fullName: String?): Result<Unit> = runCatching {
        val response = api.register(RegisterRequest(email = email, password = password, fullName = fullName))
        context.authStore.edit { it[KEY_TOKEN] = response.accessToken }
    }

    suspend fun logout() {
        context.authStore.edit { it.remove(KEY_TOKEN) }
    }

    suspend fun getToken(): String? {
        var token: String? = null
        context.authStore.data.collect { token = it[KEY_TOKEN] }
        return token
    }
}
