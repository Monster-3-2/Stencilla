package com.stencilla.app.data.local.db

import androidx.room.*

@Dao
interface OutfitFeedbackDao {
    @Query("SELECT * FROM outfit_feedback ORDER BY createdAt DESC LIMIT :n")
    suspend fun recent(n: Int): List<OutfitFeedbackEntity>

    @Query("SELECT * FROM outfit_feedback WHERE outfitId = :id")
    suspend fun getByOutfitId(id: String): OutfitFeedbackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(feedback: OutfitFeedbackEntity)

    @Delete
    suspend fun delete(feedback: OutfitFeedbackEntity)
}
