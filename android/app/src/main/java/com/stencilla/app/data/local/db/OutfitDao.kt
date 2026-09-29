package com.stencilla.app.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OutfitEntity>>

    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    suspend fun getAll(): List<OutfitEntity>

    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getById(id: String): OutfitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outfit: OutfitEntity)

    @Delete
    suspend fun delete(outfit: OutfitEntity)

    @Query("DELETE FROM outfits WHERE id NOT IN (SELECT id FROM outfits ORDER BY createdAt DESC LIMIT 50)")
    suspend fun pruneOldOutfits()
}
