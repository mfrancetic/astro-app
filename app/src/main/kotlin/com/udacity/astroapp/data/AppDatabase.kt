package com.udacity.astroapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.Converters

@Database(
    entities = [Photo::class, Asteroid::class, Observatory::class, EarthPhoto::class, MarsPhoto::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun astroDao(): AstroDao

    companion object {
        private const val DATABASE_NAME = "astroAppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}