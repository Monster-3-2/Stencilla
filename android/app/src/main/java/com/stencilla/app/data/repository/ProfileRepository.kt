package com.stencilla.app.data.repository

import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.ProfileResponse
import com.stencilla.app.data.remote.dto.ProfileUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getProfile(): ProfileResponse = api.getProfile()

    suspend fun updateProfile(request: ProfileUpdateRequest): ProfileResponse = api.updateProfile(request)
}
