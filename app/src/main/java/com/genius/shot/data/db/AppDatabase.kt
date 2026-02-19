package com.genius.shot.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageLabelEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageLabelDao(): ImageLabelDao
}