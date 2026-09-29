package com.stencilla.app.data.repository

import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.local.db.OutfitDao
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.WardrobeAnalyticsRequestDto
import com.stencilla.app.data.remote.dto.WardrobeAnalyticsResponseDto
import com.stencilla.app.data.remote.dto.WardrobeItemInputDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeAnalyticsRepository @Inject constructor(
    private val api: ApiService,
    private val clothingDao: ClothingItemDao,
    private val outfitDao: OutfitDao,
) {
    private fun buildItems(): List<WardrobeItemInputDto> =
        kotlinx.coroutines.runBlocking { clothingDao.getAll() }.filter { it.aiTagged }.map {
            WardrobeItemInputDto(
                id = it.id, category = it.category, subcategory = it.subcategory,
                colorPrimary = it.colorPrimary, colorSecondary = it.colorSecondary,
                pattern = it.pattern, formality = it.formality, season = it.season,
                material = it.material, fit = it.fit, brand = it.brand,
                size = it.size, condition = it.condition,
                availability = it.availability, laundryState = it.laundryState,
                repairNote = it.repairNote,
            )
        }

    suspend fun getAnalytics(): WardrobeAnalyticsResponseDto {
        val items = clothingDao.getAll().filter { it.aiTagged }.map {
            WardrobeItemInputDto(
                id = it.id, category = it.category, subcategory = it.subcategory,
                colorPrimary = it.colorPrimary, colorSecondary = it.colorSecondary,
                pattern = it.pattern, formality = it.formality, season = it.season,
                material = it.material, fit = it.fit, brand = it.brand,
                size = it.size, condition = it.condition,
                availability = it.availability, laundryState = it.laundryState,
                repairNote = it.repairNote,
            )
        }
        val outfitCount = try { outfitDao.getAll().size } catch (_: Exception) { 0 }
        return api.getWardrobeAnalytics(
            WardrobeAnalyticsRequestDto(
                items = items,
                outfitCount = outfitCount,
                totalItems = items.size,
            )
        )
    }

    suspend fun search(query: String): List<String> {
        val items = clothingDao.getAll().filter { it.aiTagged }.map {
            WardrobeItemInputDto(id = it.id, category = it.category, subcategory = it.subcategory,
                colorPrimary = it.colorPrimary, colorSecondary = it.colorSecondary,
                pattern = it.pattern, formality = it.formality, season = it.season,
                material = it.material, fit = it.fit, brand = it.brand)
        }
        return api.searchWardrobe(
            WardrobeAnalyticsRequestDto(items = items),
            query = query,
        ).itemIds
    }

    suspend fun filter(
        category: String? = null,
        formality: String? = null,
        season: String? = null,
        color: String? = null,
    ): List<String> {
        val items = clothingDao.getAll().filter { it.aiTagged }.map {
            WardrobeItemInputDto(id = it.id, category = it.category, subcategory = it.subcategory,
                colorPrimary = it.colorPrimary, colorSecondary = it.colorSecondary,
                pattern = it.pattern, formality = it.formality, season = it.season,
                material = it.material, fit = it.fit, brand = it.brand)
        }
        return api.filterWardrobe(
            WardrobeAnalyticsRequestDto(items = items),
            category = category, formality = formality, season = season, color = color,
        ).itemIds
    }
}
