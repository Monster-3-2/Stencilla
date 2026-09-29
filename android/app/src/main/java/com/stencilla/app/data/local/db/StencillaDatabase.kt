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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS outfits (
                id TEXT NOT NULL, occasion TEXT NOT NULL, itemIds TEXT NOT NULL,
                reasoning TEXT, shoppingSuggestionsJson TEXT NOT NULL,
                avatarDescription TEXT, createdAt INTEGER NOT NULL, PRIMARY KEY(id)
            )""".trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS outfit_feedback (
                outfitId TEXT NOT NULL, outcome TEXT NOT NULL, rating INTEGER,
                reason TEXT, createdAt INTEGER NOT NULL, PRIMARY KEY(outfitId)
            )""".trimIndent())
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN brand TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN size TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN condition TEXT")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN availability TEXT NOT NULL DEFAULT 'available'")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN laundryState TEXT NOT NULL DEFAULT 'clean'")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN additionalPhotoPaths TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN repairNote TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN wearCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN lastWornAt INTEGER")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE clothing_items ADD COLUMN purchasePriceCents INTEGER")
    }
}

// NEW: Style notes and planner events stored locally for offline access
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS style_notes (
                id INTEGER PRIMARY KEY NOT NULL,
                serverId INTEGER,
                title TEXT NOT NULL,
                body TEXT,
                tags TEXT,
                synced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )""".trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS planner_events (
                id INTEGER PRIMARY KEY NOT NULL,
                serverId INTEGER,
                title TEXT NOT NULL,
                eventDate TEXT NOT NULL,
                eventTime TEXT,
                occasion TEXT,
                dressCode TEXT,
                notes TEXT,
                plannedOutfitIds TEXT,
                isTrip INTEGER NOT NULL DEFAULT 0,
                tripEndDate TEXT,
                synced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )""".trimIndent())
    }
}

@Database(
    entities = [
        ClothingItemEntity::class,
        OutfitEntity::class,
        OutfitFeedbackEntity::class,
        StyleNoteEntity::class,
        PlannerEventEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class StencillaDatabase : RoomDatabase() {
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun outfitDao(): OutfitDao
    abstract fun outfitFeedbackDao(): OutfitFeedbackDao
    abstract fun styleNoteDao(): StyleNoteDao
    abstract fun plannerEventDao(): PlannerEventDao
}
