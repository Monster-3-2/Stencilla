package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WardrobeItemInputDto(
    val id: String,
    val category: String? = null,
    val subcategory: String? = null,
    @SerialName("color_primary") val colorPrimary: String? = null,
    @SerialName("color_secondary") val colorSecondary: String? = null,
    val pattern: String? = null,
    val formality: String? = null,
    val season: String? = null,
)

@Serializable
data class OutfitRequest(
    val occasion: String,
    @SerialName("wardrobe_items") val wardrobeItems: List<WardrobeItemInputDto>,
    @SerialName("anchor_item_id") val anchorItemId: String? = null,
    val notes: String? = null,
)

@Serializable
data class ShoppingSuggestionDto(
    val item: String,
    val reason: String,
)

@Serializable
data class OutfitResponse(
    val occasion: String,
    @SerialName("item_ids") val itemIds: List<String>,
    val reasoning: String? = null,
    @SerialName("shopping_suggestions") val shoppingSuggestions: List<ShoppingSuggestionDto> = emptyList(),
    @SerialName("avatar_description") val avatarDescription: String? = null,
)
