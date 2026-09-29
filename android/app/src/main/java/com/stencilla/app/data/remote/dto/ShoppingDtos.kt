package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ShoppingGapRequestDto(
    val gaps: List<String>,
    @SerialName("wardrobe_items") val wardrobeItems: List<Map<String, String>> = emptyList(),
    val measurements: Map<String, JsonElement> = emptyMap(),
    val region: String = "IN",
    val size: String = "M",
    val budget: Double = 5000.0,
    val retailers: List<Map<String, JsonElement>> = emptyList(),
)

@Serializable
data class ShoppingGapResponseDto(
    val recommendations: List<ShoppingRecommendationDto> = emptyList(),
    val rejected: Map<String, List<String>> = emptyMap(),
    @SerialName("verified_at") val verifiedAt: String = "",
)

@Serializable
data class ShoppingRecommendationDto(
    val gap: String,
    val retailer: String,
    val name: String,
    val url: String,
    val price: Double,
    val currency: String = "INR",
    val size: String = "",
    @SerialName("measurement_match") val measurementMatch: Boolean = true,
    val verification: List<String> = emptyList(),
)
