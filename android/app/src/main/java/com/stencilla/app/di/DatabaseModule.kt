package com.stencilla.app.di

import android.content.Context
import androidx.room.Room
import com.stencilla.app.data.local.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): StencillaDatabase =
        Room.databaseBuilder(ctx, StencillaDatabase::class.java, "stencilla.db")
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideClothingItemDao(db: StencillaDatabase): ClothingItemDao = db.clothingItemDao()
    @Provides fun provideOutfitDao(db: StencillaDatabase): OutfitDao = db.outfitDao()
    @Provides fun provideOutfitFeedbackDao(db: StencillaDatabase): OutfitFeedbackDao = db.outfitFeedbackDao()
    @Provides fun provideStyleNoteDao(db: StencillaDatabase): StyleNoteDao = db.styleNoteDao()
    @Provides fun providePlannerEventDao(db: StencillaDatabase): PlannerEventDao = db.plannerEventDao()
}
