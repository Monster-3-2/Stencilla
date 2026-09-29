package com.stencilla.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val occasion: String,
    val itemIds: String,            // comma-separated
    val reasoning: String? = null,
    val shoppingSuggestionsJson: String = "[]",
    val avatarDescription: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
