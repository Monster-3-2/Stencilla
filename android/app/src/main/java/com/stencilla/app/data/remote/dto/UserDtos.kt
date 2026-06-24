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
)
