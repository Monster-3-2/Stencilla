package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClothingTagResponse(
    val category: String? = null,
    val subcategory: String? = null,
    @SerialName("color_primary") val colorPrimary: String? = null,
    @SerialName("color_secondary") val colorSecondary: String? = null,
    val pattern: String? = null,
    val formality: String? = null,
    val season: String? = null,
    val material: String? = null,
    val fit: String? = null,
    @SerialName("needs_clarification") val needsClarification: Boolean = false,
    @SerialName("clarification_question") val clarificationQuestion: String? = null,
    @SerialName("ai_image_description") val aiImageDescription: String? = null,
)
