package com.stencilla.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfit_feedback")
data class OutfitFeedbackEntity(
    @PrimaryKey val outfitId: String,
    val outcome: String,        // "worn" | "skipped"
    val rating: Int? = null,    // 1-5
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
