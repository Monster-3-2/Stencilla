package com.stencilla.app.data.remote

import com.stencilla.app.data.remote.dto.ClothingTagResponse
import com.stencilla.app.data.remote.dto.LoginRequest
import com.stencilla.app.data.remote.dto.OutfitRequest
import com.stencilla.app.data.remote.dto.OutfitResponse
import com.stencilla.app.data.remote.dto.ProfileResponse
import com.stencilla.app.data.remote.dto.ProfileUpdateRequest
import com.stencilla.app.data.remote.dto.RegisterRequest
import com.stencilla.app.data.remote.dto.TokenResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("users/me")
    suspend fun getProfile(): ProfileResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ProfileResponse

    /** Stateless: uploads a photo, gets structured tags back. Nothing is stored server-side -
     * the client persists the image and these tags locally (see data.local.db). */
    @Multipart
    @POST("wardrobe/tag")
    suspend fun tagClothingItem(@Part image: MultipartBody.Part): ClothingTagResponse

    /** Stateless: the client sends its local wardrobe + profile context, gets back which
     * item ids form a coherent outfit. No history is stored server-side. */
    @POST("outfits/suggest")
    suspend fun suggestOutfit(@Body request: OutfitRequest): OutfitResponse
}
