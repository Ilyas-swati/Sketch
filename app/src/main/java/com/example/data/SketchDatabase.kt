package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SketchProjectEntity::class], version = 1, exportSchema = false)
abstract class SketchDatabase : RoomDatabase() {
    abstract fun sketchDao(): SketchDao

    companion object {
        @Volatile
        private var INSTANCE: SketchDatabase? = null

        fun getInstance(context: Context): SketchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SketchDatabase::class.java,
                    "line_sketch_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
