package com.stencilla.app.data.remote

import com.stencilla.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    // ─── Profile ─────────────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getProfile(): ProfileResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ProfileResponse

    // ─── Settings ────────────────────────────────────────────────────────────
    @GET("settings")
    suspend fun getSettings(): SettingsDto

    @PUT("settings")
    suspend fun updateSettings(@Body dto: SettingsDto): SettingsDto

    // ─── Wardrobe ────────────────────────────────────────────────────────────
    @Multipart
    @POST("wardrobe/tag")
    suspend fun tagClothingItem(@Part image: MultipartBody.Part): ClothingTagResponse

    @POST("wardrobe/analytics")
    suspend fun getWardrobeAnalytics(@Body request: WardrobeAnalyticsRequestDto): WardrobeAnalyticsResponseDto

    @POST("wardrobe/search")
    suspend fun searchWardrobe(
        @Body request: WardrobeAnalyticsRequestDto,
        @Query("query") query: String,
    ): WardrobeSearchResponseDto

    @POST("wardrobe/filter")
    suspend fun filterWardrobe(
        @Body request: WardrobeAnalyticsRequestDto,
        @Query("category") category: String? = null,
        @Query("formality") formality: String? = null,
        @Query("season") season: String? = null,
        @Query("color") color: String? = null,
    ): WardrobeFilterResponseDto

    @POST("wardrobe/ai-suggestion")
    suspend fun getAiWardrobeSuggestion(@Body request: WardrobeAnalyticsRequestDto): Map<String, String>

    // ─── Outfits ─────────────────────────────────────────────────────────────
    @POST("outfits/suggest")
    suspend fun suggestOutfit(@Body request: OutfitRequest): OutfitResponse

    // ─── Context ─────────────────────────────────────────────────────────────
    @GET("context/weather")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
    ): WeatherResponse

    @GET("context/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("days") days: Int = 7,
    ): ForecastResponse

    // ─── Verifier ────────────────────────────────────────────────────────────
    @Multipart
    @POST("verifier/analyze")
    suspend fun analyzeOutfit(
        @Part image: MultipartBody.Part,
        @Part("consent") consent: RequestBody,
        @Part("retain_on_server") retainOnServer: RequestBody,
    ): OutfitVerificationResponse

    // ─── Shopping ────────────────────────────────────────────────────────────
    @POST("shopping/gaps")
    suspend fun verifyShoppingGaps(@Body request: ShoppingGapRequestDto): ShoppingGapResponseDto

    // ─── Style Notes ─────────────────────────────────────────────────────────
    @GET("stylenotes")
    suspend fun listStyleNotes(): List<StyleNoteDto>

    @POST("stylenotes")
    suspend fun createStyleNote(@Body request: StyleNoteCreateDto): StyleNoteDto

    @PUT("stylenotes/{id}")
    suspend fun updateStyleNote(@Path("id") id: Int, @Body request: StyleNoteUpdateDto): StyleNoteDto

    @DELETE("stylenotes/{id}")
    suspend fun deleteStyleNote(@Path("id") id: Int)

    // ─── Planner ─────────────────────────────────────────────────────────────
    @GET("planner/events")
    suspend fun listEvents(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
    ): List<PlannerEventDto>

    @GET("planner/events/upcoming")
    suspend fun upcomingEvents(@Query("days") days: Int = 7): List<PlannerEventDto>

    @GET("planner/calendar")
    suspend fun calendarStrip(@Query("year") year: Int, @Query("month") month: Int): List<PlannerEventDto>

    @POST("planner/events")
    suspend fun createEvent(@Body request: PlannerEventCreateDto): PlannerEventDto

    @PUT("planner/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body request: PlannerEventUpdateDto): PlannerEventDto

    @DELETE("planner/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int)

    @POST("planner/events/{id}/outfit")
    suspend fun outfitForEvent(
        @Path("id") id: Int,
        @Body request: OutfitForEventRequestDto,
    ): OutfitForEventResponseDto
}
