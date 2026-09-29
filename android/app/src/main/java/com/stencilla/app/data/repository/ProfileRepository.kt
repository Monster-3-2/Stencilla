package com.stencilla.app.data.repository

import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.ProfileResponse
import com.stencilla.app.data.remote.dto.ProfileUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Alias kept for backward compatibility with ProfileViewModel.
 * Delegates to ApiService directly.
 */
@Singleton
class ProfileRepository @Inject constructor(private val api: ApiService) {
    suspend fun getProfile(): Result<ProfileResponse> = runCatching { api.getProfile() }
    suspend fun saveProfile(request: ProfileUpdateRequest): Result<ProfileResponse> =
        runCatching { api.updateProfile(request) }
}
