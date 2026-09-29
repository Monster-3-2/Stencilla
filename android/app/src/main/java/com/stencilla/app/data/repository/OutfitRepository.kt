package com.stencilla.app.data.repository

import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.local.db.OutfitDao
import com.stencilla.app.data.local.db.OutfitEntity
import com.stencilla.app.data.local.db.OutfitFeedbackDao
import com.stencilla.app.data.local.db.OutfitFeedbackEntity
import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.OutfitRequest
import com.stencilla.app.data.remote.dto.OutfitResponse
import com.stencilla.app.data.remote.dto.ShoppingSuggestionDto
import com.stencilla.app.data.remote.dto.WardrobeItemInputDto
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitRepository @Inject constructor(
    private val api: ApiService,
    private val outfitDao: OutfitDao,
    private val feedbackDao: OutfitFeedbackDao,
    private val clothingDao: ClothingItemDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun observeHistory(): Flow<List<OutfitEntity>> = outfitDao.observeAll()

    suspend fun saveOutfit(response: OutfitResponse): OutfitEntity {
        val outfit = OutfitEntity(
                occasion = response.occasion,
                itemIds = response.itemIds.joinToString(","),
                reasoning = response.reasoning,
                shoppingSuggestionsJson = json.encodeToString(response.shoppingSuggestions),
                avatarDescription = response.avatarDescription,
            )
        outfitDao.insert(outfit)
        return outfit
    }

    suspend fun saveFeedback(outfitId: String, outcome: String, rating: Int?, reason: String?) {
        val previous = feedbackDao.getByOutfitId(outfitId)
        feedbackDao.upsert(OutfitFeedbackEntity(outfitId, outcome, rating, reason?.trim()?.takeIf { it.isNotEmpty() }))
        if (outcome == "worn" && previous?.outcome != "worn") {
            val outfit = outfitDao.getById(outfitId)
            val wornAt = System.currentTimeMillis()
            outfit?.itemIds?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.forEach {
                clothingDao.recordWear(it, wornAt)
            }
        }
    }

    suspend fun preferenceSummary(): String? {
        val feedback = feedbackDao.recent(30)
        if (feedback.isEmpty()) return null
        val worn = feedback.count { it.outcome == "worn" }
        val skipped = feedback.count { it.outcome == "skipped" }
        val rated = feedback.mapNotNull { it.rating }
        val average = rated.takeIf { it.isNotEmpty() }?.average()?.let { "%.1f".format(it) }
        val reasons = feedback.mapNotNull { it.reason }.distinct().take(5)
        val rotation = clothingDao.getAllForAnalytics()
        val neglected = rotation.count { it.wearCount == 0 && it.availability == "available" }
        val recentlyWorn = rotation.count { it.lastWornAt != null && it.lastWornAt > System.currentTimeMillis() - 7 * 86_400_000L }
        return buildString {
            append("Recent feedback: worn=$worn, skipped=$skipped")
            average?.let { append(", average_rating=$it/5") }
            if (reasons.isNotEmpty()) append(", reasons=${reasons.joinToString("; ")}")
            append(", rotation: neglected=$neglected, recently_worn=$recentlyWorn")
            val avoid = rotation.filter { it.lastWornAt != null && it.lastWornAt > System.currentTimeMillis() - 2 * 86_400_000L }
            if (avoid.isNotEmpty()) append(", avoid_repeat_ids=${avoid.joinToString { it.id }}")
        }.take(500)
    }

    fun shoppingSuggestions(outfit: OutfitEntity): List<ShoppingSuggestionDto> =
        runCatching {
            json.decodeFromString<List<ShoppingSuggestionDto>>(outfit.shoppingSuggestionsJson)
        }.getOrDefault(emptyList())

    /** [wardrobe] is the caller's current local closet (from Room) - sent fresh on every
     * call since the backend keeps no wardrobe state of its own. */
    suspend fun suggestOutfit(
        occasion: String,
        dressCode: String? = null,
        wardrobe: List<ClothingItemEntity>,
        anchorItemId: String?,
        notes: String?,
        weatherTemp: Double? = null,
        weatherCondition: String? = null,
        calendarEvent: String? = null,
        preferenceSummary: String? = null,
    ): OutfitResponse {
        val items = wardrobe.map {
            WardrobeItemInputDto(
                id = it.id,
                category = it.category,
                subcategory = it.subcategory,
                colorPrimary = it.colorPrimary,
                colorSecondary = it.colorSecondary,
                pattern = it.pattern,
                formality = it.formality,
                season = it.season,
                material = it.material,
                fit = it.fit,
                brand = it.brand,
                size = it.size,
                condition = it.condition,
                availability = it.availability,
                laundryState = it.laundryState,
                repairNote = it.repairNote,
            )
        }
        return api.suggestOutfit(
            OutfitRequest(
                occasion = occasion,
                dressCode = dressCode,
                wardrobeItems = items,
                anchorItemId = anchorItemId,
                notes = notes,
                weatherTemp = weatherTemp,
                weatherCondition = weatherCondition,
                calendarEvent = calendarEvent,
                preferenceSummary = preferenceSummary,
            ),
        )
    }
}
