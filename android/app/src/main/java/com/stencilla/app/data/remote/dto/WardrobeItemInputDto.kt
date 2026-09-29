package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WardrobeItemInputDto(
    val id: String,
    val category: String? = null,
    val subcategory: String? = null,
    @SerialName("color_primary")    val colorPrimary: String? = null,
    @SerialName("color_secondary")  val colorSecondary: String? = null,
    val pattern: String? = null,
    val formality: String? = null,
    val season: String? = null,
    val material: String? = null,
    val fit: String? = null,
    val brand: String? = null,
    val size: String? = null,
    val condition: String? = null,
    val availability: String? = null,
    @SerialName("laundry_state")    val laundryState: String? = null,
    @SerialName("repair_note")      val repairNote: String? = null,
)
