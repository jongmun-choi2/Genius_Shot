package com.genius.shot.di

import android.content.Context
import androidx.room.Room
import com.genius.shot.data.db.AppDatabase
import com.genius.shot.data.db.ImageLabelDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "genius_shot.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideImageLabelDao(db: AppDatabase): ImageLabelDao = db.imageLabelDao()
}