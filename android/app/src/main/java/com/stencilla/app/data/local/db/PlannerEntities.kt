package com.stencilla.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "style_notes")
data class StyleNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverId: Int? = null,
    val title: String,
    val body: String? = null,
    val tags: String? = null,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "planner_events")
data class PlannerEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverId: Int? = null,
    val title: String,
    val eventDate: String,          // YYYY-MM-DD
    val eventTime: String? = null,  // HH:MM
    val occasion: String? = null,
    val dressCode: String? = null,
    val notes: String? = null,
    val plannedOutfitIds: String? = null,
    val isTrip: Boolean = false,
    val tripEndDate: String? = null,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
