package com.stencilla.app.data.repository

import com.stencilla.app.data.local.db.StyleNoteDao
import com.stencilla.app.data.local.db.StyleNoteEntity
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.StyleNoteCreateDto
import com.stencilla.app.data.remote.dto.StyleNoteUpdateDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleNotesRepository @Inject constructor(
    private val api: ApiService,
    private val dao: StyleNoteDao,
) {
    fun observeNotes(): Flow<List<StyleNoteEntity>> = dao.observeAll()

    /** Pull latest from server and merge into local DB. */
    suspend fun sync() {
        try {
            val remote = api.listStyleNotes()
            remote.forEach { dto ->
                val existing = dao.getAll().firstOrNull { it.serverId == dto.id }
                val entity = StyleNoteEntity(
                    id = existing?.id ?: 0,
                    serverId = dto.id,
                    title = dto.title,
                    body = dto.body,
                    tags = dto.tags,
                    synced = true,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                dao.insert(entity)
            }
        } catch (_: Exception) {
            // Offline — local data stays intact
        }
    }

    suspend fun create(title: String, body: String?, tags: String?): StyleNoteEntity {
        // Optimistic local insert
        val localId = dao.insert(
            StyleNoteEntity(title = title, body = body, tags = tags, synced = false)
        ).toInt()
        val local = dao.getById(localId)!!
        // Try remote
        return try {
            val dto = api.createStyleNote(StyleNoteCreateDto(title = title, body = body, tags = tags))
            val synced = local.copy(serverId = dto.id, synced = true)
            dao.update(synced)
            synced
        } catch (_: Exception) {
            local
        }
    }

    suspend fun update(entity: StyleNoteEntity, title: String, body: String?, tags: String?): StyleNoteEntity {
        val updated = entity.copy(title = title, body = body, tags = tags,
            synced = false, updatedAt = System.currentTimeMillis())
        dao.update(updated)
        return if (entity.serverId != null) {
            try {
                api.updateStyleNote(entity.serverId, StyleNoteUpdateDto(title = title, body = body, tags = tags))
                val s = updated.copy(synced = true)
                dao.update(s)
                s
            } catch (_: Exception) { updated }
        } else updated
    }

    suspend fun delete(entity: StyleNoteEntity) {
        dao.delete(entity)
        if (entity.serverId != null) {
            try { api.deleteStyleNote(entity.serverId) } catch (_: Exception) { /* queue for retry */ }
        }
    }
}
