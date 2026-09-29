package com.stencilla.app.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleNoteDao {
    @Query("SELECT * FROM style_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<StyleNoteEntity>>

    @Query("SELECT * FROM style_notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<StyleNoteEntity>

    @Query("SELECT * FROM style_notes WHERE id = :id")
    suspend fun getById(id: Int): StyleNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: StyleNoteEntity): Long

    @Update
    suspend fun update(note: StyleNoteEntity)

    @Delete
    suspend fun delete(note: StyleNoteEntity)

    @Query("DELETE FROM style_notes WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface PlannerEventDao {
    @Query("SELECT * FROM planner_events ORDER BY eventDate ASC, eventTime ASC")
    fun observeAll(): Flow<List<PlannerEventEntity>>

    @Query("SELECT * FROM planner_events ORDER BY eventDate ASC, eventTime ASC")
    suspend fun getAll(): List<PlannerEventEntity>

    @Query("SELECT * FROM planner_events WHERE eventDate >= :fromDate ORDER BY eventDate ASC, eventTime ASC")
    suspend fun getUpcoming(fromDate: String): List<PlannerEventEntity>

    @Query("SELECT * FROM planner_events WHERE eventDate LIKE :yearMonth || '%' ORDER BY eventDate ASC")
    suspend fun getByMonth(yearMonth: String): List<PlannerEventEntity>  // pass "YYYY-MM"

    @Query("SELECT * FROM planner_events WHERE id = :id")
    suspend fun getById(id: Int): PlannerEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: PlannerEventEntity): Long

    @Update
    suspend fun update(event: PlannerEventEntity)

    @Delete
    suspend fun delete(event: PlannerEventEntity)

    @Query("DELETE FROM planner_events WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE planner_events SET plannedOutfitIds = :ids, synced = 0, updatedAt = :now WHERE id = :id")
    suspend fun setPlannedOutfitIds(id: Int, ids: String, now: Long = System.currentTimeMillis())
}
