package com.stencilla.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN material TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN fit TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN aiImageDescription TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN needsClarification INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN clarificationQuestion TEXT")
    }
}

@Database(entities = [ClothingItemEntity::class], version = 2, exportSchema = false)
abstract class StencillaDatabase : RoomDatabase() {
    abstract fun clothingItemDao(): ClothingItemDao
}
