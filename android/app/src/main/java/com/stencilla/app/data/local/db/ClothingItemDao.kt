package com.stencilla.app.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {

    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    suspend fun getAll(): List<ClothingItemEntity>

    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<ClothingItemEntity>>

    @Query("SELECT * FROM clothing_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ClothingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClothingItemEntity)

    @Update
    suspend fun update(item: ClothingItemEntity)

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getById(id: String): ClothingItemEntity?

    @Query("UPDATE clothing_items SET wearCount = wearCount + 1, lastWornAt = :wornAt WHERE id = :id")
    suspend fun recordWear(id: String, wornAt: Long)

    @Query("UPDATE clothing_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("SELECT * FROM clothing_items ORDER BY wearCount ASC, lastWornAt ASC")
    suspend fun getAllForAnalytics(): List<ClothingItemEntity>

    @Delete
    suspend fun delete(item: ClothingItemEntity)
}
