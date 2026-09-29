package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StyleNoteDto(
    val id: Int,
    val title: String,
    val body: String? = null,
    val tags: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class StyleNoteCreateDto(
    val title: String,
    val body: String? = null,
    val tags: String? = null,
)

@Serializable
data class StyleNoteUpdateDto(
    val title: String? = null,
    val body: String? = null,
    val tags: String? = null,
)

@Serializable
data class PlannerEventDto(
    val id: Int,
    val title: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("event_time") val eventTime: String? = null,
    val occasion: String? = null,
    @SerialName("dress_code") val dressCode: String? = null,
    val notes: String? = null,
    @SerialName("planned_outfit_ids") val plannedOutfitIds: String? = null,
    @SerialName("is_trip") val isTrip: Boolean = false,
    @SerialName("trip_end_date") val tripEndDate: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class PlannerEventCreateDto(
    val title: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("event_time") val eventTime: String? = null,
    val occasion: String? = null,
    @SerialName("dress_code") val dressCode: String? = null,
    val notes: String? = null,
    @SerialName("planned_outfit_ids") val plannedOutfitIds: String? = null,
    @SerialName("is_trip") val isTrip: Boolean = false,
    @SerialName("trip_end_date") val tripEndDate: String? = null,
)

@Serializable
data class PlannerEventUpdateDto(
    val title: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("event_time") val eventTime: String? = null,
    val occasion: String? = null,
    @SerialName("dress_code") val dressCode: String? = null,
    val notes: String? = null,
    @SerialName("planned_outfit_ids") val plannedOutfitIds: String? = null,
    @SerialName("is_trip") val isTrip: Boolean? = null,
    @SerialName("trip_end_date") val tripEndDate: String? = null,
)

@Serializable
data class OutfitForEventRequestDto(
    @SerialName("event_id") val eventId: Int,
    @SerialName("wardrobe_items") val wardrobeItems: List<WardrobeItemInputDto>,
)

@Serializable
data class OutfitForEventResponseDto(
    @SerialName("item_ids") val itemIds: List<String>,
    val reasoning: String? = null,
    @SerialName("avatar_description") val avatarDescription: String? = null,
)

// Analytics
@Serializable
data class WardrobeAnalyticsRequestDto(
    val items: List<WardrobeItemInputDto>,
    @SerialName("outfit_count") val outfitCount: Int = 0,
    @SerialName("total_items") val totalItems: Int = 0,
)

@Serializable
data class CategoryCountDto(val category: String, val count: Int)

@Serializable
data class ColorEntryDto(val color: String, val count: Int)

@Serializable
data class SmartCategoryDto(val label: String, val count: Int, @SerialName("item_ids") val itemIds: List<String>)

@Serializable
data class WardrobeAnalyticsResponseDto(
    @SerialName("total_items") val totalItems: Int,
    @SerialName("outfit_count") val outfitCount: Int,
    @SerialName("utilization_pct") val utilizationPct: Int,
    @SerialName("top_colors") val topColors: List<ColorEntryDto>,
    @SerialName("category_breakdown") val categoryBreakdown: List<CategoryCountDto>,
    @SerialName("smart_categories") val smartCategories: List<SmartCategoryDto>,
    @SerialName("ai_suggestion") val aiSuggestion: String,
)

// Settings
@Serializable
data class SettingsDto(
    @SerialName("notification_outfit") val notificationOutfit: Boolean = true,
    @SerialName("notification_planner") val notificationPlanner: Boolean = true,
    @SerialName("notification_tips") val notificationTips: Boolean = true,
    @SerialName("privacy_analytics") val privacyAnalytics: Boolean = true,
)

@Serializable
data class WardrobeSearchResponseDto(
    @SerialName("item_ids") val itemIds: List<String>,
    val count: Int,
)

@Serializable
data class WardrobeFilterResponseDto(
    @SerialName("item_ids") val itemIds: List<String>,
    val count: Int,
    @SerialName("filter_applied") val filterApplied: Map<String, String?> = emptyMap(),
)
