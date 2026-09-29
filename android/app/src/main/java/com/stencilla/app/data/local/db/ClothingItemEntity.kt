package com.stencilla.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey val id: String,
    val localImagePath: String,
    val onDeviceLabel: String? = null,
    val category: String? = null,       // "shirt", "jeans", "jacket", etc.
    val subcategory: String? = null,    // "slim-fit Oxford shirt"
    val colorPrimary: String? = null,
    val colorSecondary: String? = null,
    val pattern: String? = null,
    val formality: String? = null,
    val season: String? = null,
    // NEW v2 fields
    val material: String? = null,       // "cotton", "denim", "leather", etc.
    val fit: String? = null,            // "slim", "regular", "oversized", etc.
    val brand: String? = null,
    val size: String? = null,
    val condition: String? = null,
    val availability: String = "available",
    val laundryState: String = "clean",
    val additionalPhotoPaths: String = "",
    val repairNote: String? = null,
    val aiImageDescription: String? = null, // AI plain-bg description for display card
    val needsClarification: Boolean = false,
    val clarificationQuestion: String? = null,
    val aiTagged: Boolean = false,
    val wearCount: Int = 0,
    val lastWornAt: Long? = null,
    val isFavorite: Boolean = false,
    val purchasePriceCents: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
