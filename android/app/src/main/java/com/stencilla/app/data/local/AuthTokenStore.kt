package com.stencilla.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(
    name = "stencilla_auth",
)

@Singleton
class AuthTokenStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val tokenKey = stringPreferencesKey("access_token")

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { it[tokenKey] }

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clearToken() {
        context.authDataStore.edit { it.remove(tokenKey) }
    }

    /** Synchronous read for use inside the OkHttp interceptor, which is itself synchronous. */
    fun getTokenBlocking(): String? = runBlocking { tokenFlow.first() }
}
