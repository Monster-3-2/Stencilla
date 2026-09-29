package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdateRequest(
    @SerialName("full_name") val fullName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val lifestyle: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    @SerialName("body_type") val bodyType: String? = null,
    @SerialName("skin_tone") val skinTone: String? = null,
    @SerialName("style_goal") val styleGoal: String? = null,
    @SerialName("preferred_colors") val preferredColors: String? = null,
    @SerialName("preferred_fits") val preferredFits: String? = null,
    @SerialName("preferred_silhouettes") val preferredSilhouettes: String? = null,
    @SerialName("comfort_priority") val comfortPriority: String? = null,
    @SerialName("formality_preference") val formalityPreference: String? = null,
    @SerialName("modesty_preference") val modestyPreference: String? = null,
    @SerialName("style_inspirations") val styleInspirations: String? = null,
    @SerialName("confidence_signals") val confidenceSignals: String? = null,
)

@Serializable
data class ProfileResponse(
    val id: Int,
    val email: String,
    @SerialName("full_name") val fullName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val lifestyle: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    @SerialName("body_type") val bodyType: String? = null,
    @SerialName("skin_tone") val skinTone: String? = null,
    @SerialName("style_goal") val styleGoal: String? = null,
    @SerialName("preferred_colors") val preferredColors: String? = null,
    @SerialName("preferred_fits") val preferredFits: String? = null,
    @SerialName("preferred_silhouettes") val preferredSilhouettes: String? = null,
    @SerialName("comfort_priority") val comfortPriority: String? = null,
    @SerialName("formality_preference") val formalityPreference: String? = null,
    @SerialName("modesty_preference") val modestyPreference: String? = null,
    @SerialName("style_inspirations") val styleInspirations: String? = null,
    @SerialName("confidence_signals") val confidenceSignals: String? = null,
)
