package com.stencilla.app.data.repository

import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.local.db.PlannerEventDao
import com.stencilla.app.data.local.db.PlannerEventEntity
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlannerRepository @Inject constructor(
    private val api: ApiService,
    private val dao: PlannerEventDao,
    private val clothingDao: ClothingItemDao,
) {
    fun observeAll(): Flow<List<PlannerEventEntity>> = dao.observeAll()

    suspend fun getUpcoming(days: Int = 7): List<PlannerEventEntity> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dao.getUpcoming(today)
    }

    suspend fun getByMonth(year: Int, month: Int): List<PlannerEventEntity> {
        val prefix = "%04d-%02d".format(year, month)
        return dao.getByMonth(prefix)
    }

    /** Sync upcoming events from server. */
    suspend fun sync() {
        try {
            val remote = api.upcomingEvents(days = 30)
            remote.forEach { dto ->
                val existing = dao.getAll().firstOrNull { it.serverId == dto.id }
                dao.insert(dtoToEntity(dto, existing?.id ?: 0))
            }
        } catch (_: Exception) { /* offline */ }
    }

    suspend fun create(
        title: String, eventDate: String, eventTime: String?,
        occasion: String?, dressCode: String?, notes: String?,
        isTrip: Boolean, tripEndDate: String?,
    ): PlannerEventEntity {
        val localId = dao.insert(
            PlannerEventEntity(
                title = title, eventDate = eventDate, eventTime = eventTime,
                occasion = occasion, dressCode = dressCode, notes = notes,
                isTrip = isTrip, tripEndDate = tripEndDate, synced = false,
            )
        ).toInt()
        val local = dao.getById(localId)!!
        return try {
            val dto = api.createEvent(PlannerEventCreateDto(
                title = title, eventDate = eventDate, eventTime = eventTime,
                occasion = occasion, dressCode = dressCode, notes = notes,
                isTrip = isTrip, tripEndDate = tripEndDate,
            ))
            val synced = local.copy(serverId = dto.id, synced = true)
            dao.update(synced)
            synced
        } catch (_: Exception) { local }
    }

    suspend fun update(entity: PlannerEventEntity): PlannerEventEntity {
        val updated = entity.copy(synced = false, updatedAt = System.currentTimeMillis())
        dao.update(updated)
        return if (entity.serverId != null) {
            try {
                api.updateEvent(entity.serverId, PlannerEventUpdateDto(
                    title = entity.title, eventDate = entity.eventDate, eventTime = entity.eventTime,
                    occasion = entity.occasion, dressCode = entity.dressCode, notes = entity.notes,
                    isTrip = entity.isTrip, tripEndDate = entity.tripEndDate,
                ))
                val s = updated.copy(synced = true)
                dao.update(s)
                s
            } catch (_: Exception) { updated }
        } else updated
    }

    suspend fun delete(entity: PlannerEventEntity) {
        dao.delete(entity)
        if (entity.serverId != null) {
            try { api.deleteEvent(entity.serverId) } catch (_: Exception) { }
        }
    }

    /** Request AI outfit suggestion for an event and persist the chosen item IDs. */
    suspend fun planOutfitForEvent(eventId: Int): OutfitForEventResponseDto? {
        val event = dao.getById(eventId) ?: return null
        val serverId = event.serverId ?: return null
        val wardrobe = clothingDao.getAll().filter { it.aiTagged }.map {
            WardrobeItemInputDto(
                id = it.id, category = it.category, subcategory = it.subcategory,
                colorPrimary = it.colorPrimary, colorSecondary = it.colorSecondary,
                pattern = it.pattern, formality = it.formality, season = it.season,
                material = it.material, fit = it.fit, brand = it.brand,
                size = it.size, condition = it.condition,
                availability = it.availability, laundryState = it.laundryState,
                repairNote = it.repairNote,
            )
        }
        return try {
            val response = api.outfitForEvent(
                serverId,
                OutfitForEventRequestDto(eventId = serverId, wardrobeItems = wardrobe),
            )
            dao.setPlannedOutfitIds(eventId, response.itemIds.joinToString(","))
            response
        } catch (_: Exception) { null }
    }

    /** Build context string of upcoming events for AI outfit calls. */
    suspend fun upcomingEventContext(): String? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val events = dao.getUpcoming(today).take(5)
        if (events.isEmpty()) return null
        return events.joinToString("; ") { evt ->
            buildString {
                append(evt.title)
                if (evt.occasion != null) append(" (${evt.occasion})")
                append(" on ${evt.eventDate}")
                if (evt.dressCode != null) append(", dress code: ${evt.dressCode}")
            }
        }
    }

    private fun dtoToEntity(dto: PlannerEventDto, localId: Int): PlannerEventEntity =
        PlannerEventEntity(
            id = localId, serverId = dto.id, title = dto.title, eventDate = dto.eventDate,
            eventTime = dto.eventTime, occasion = dto.occasion, dressCode = dto.dressCode,
            notes = dto.notes, plannedOutfitIds = dto.plannedOutfitIds, isTrip = dto.isTrip,
            tripEndDate = dto.tripEndDate, synced = true, updatedAt = System.currentTimeMillis(),
        )
}
