package com.stencilla.app.di

import android.content.Context
import androidx.room.Room
import com.stencilla.app.data.local.db.ClothingItemDao
import com.stencilla.app.data.local.db.MIGRATION_1_2
import com.stencilla.app.data.local.db.StencillaDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): StencillaDatabase {
        return Room.databaseBuilder(context, StencillaDatabase::class.java, "stencilla.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideClothingItemDao(database: StencillaDatabase): ClothingItemDao = database.clothingItemDao()
}
