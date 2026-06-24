package com.stencilla.app.data.repository

import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.OutfitRequest
import com.stencilla.app.data.remote.dto.OutfitResponse
import com.stencilla.app.data.remote.dto.WardrobeItemInputDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitRepository @Inject constructor(
    private val api: ApiService,
) {
    /** [wardrobe] is the caller's current local closet (from Room) - sent fresh on every
     * call since the backend keeps no wardrobe state of its own. */
    suspend fun suggestOutfit(
        occasion: String,
        wardrobe: List<ClothingItemEntity>,
        anchorItemId: String?,
        notes: String?,
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
            )
        }
        return api.suggestOutfit(
            OutfitRequest(occasion = occasion, wardrobeItems = items, anchorItemId = anchorItemId, notes = notes),
        )
    }
}
