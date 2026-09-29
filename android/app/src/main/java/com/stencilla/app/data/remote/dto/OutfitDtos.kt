package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutfitRequest(
    val occasion: String,
    @SerialName("dress_code") val dressCode: String? = null,
    @SerialName("wardrobe_items") val wardrobeItems: List<WardrobeItemInputDto>,
    @SerialName("anchor_item_id") val anchorItemId: String? = null,
    val notes: String? = null,
    @SerialName("weather_temp") val weatherTemp: Double? = null,
    @SerialName("weather_condition") val weatherCondition: String? = null,
    @SerialName("calendar_event") val calendarEvent: String? = null,
    @SerialName("preference_summary") val preferenceSummary: String? = null,
)

@Serializable
data class ShoppingSuggestionDto(
    val item: String,
    val reason: String,
)

@Serializable
data class OutfitFactorDto(
    val label: String,
    val score: Int,
    val explanation: String,
)

@Serializable
data class OutfitAlternativeDto(
    @SerialName("item_ids") val itemIds: List<String>,
    val label: String,
    val reason: String,
)

@Serializable
data class OutfitResponse(
    val occasion: String,
    @SerialName("item_ids") val itemIds: List<String>,
    val reasoning: String? = null,
    @SerialName("shopping_suggestions") val shoppingSuggestions: List<ShoppingSuggestionDto> = emptyList(),
    @SerialName("avatar_description") val avatarDescription: String? = null,
    val factors: List<OutfitFactorDto> = emptyList(),
    val alternatives: List<OutfitAlternativeDto> = emptyList(),
)

@Serializable
data class OutfitVerificationResponse(
    @SerialName("fit_feedback") val fitFeedback: String = "",
    @SerialName("color_feedback") val colorFeedback: String = "",
    @SerialName("proportion_feedback") val proportionFeedback: String = "",
    @SerialName("overall_feedback") val overallFeedback: String = "",
    val cautions: List<String> = emptyList(),
)
