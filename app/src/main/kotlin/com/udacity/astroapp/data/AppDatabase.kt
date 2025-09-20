package com.udacity.astroapp.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.Converters

@Database(
    entities =
        [Photo::class, Asteroid::class, Observatory::class, EarthPhoto::class, MarsPhoto::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun astroDao(): AstroDao

    companion object {
        private const val LOG_TAG = "AppDatabase"
        private const val DATABASE_NAME = "astroAppDatabase"
        @Volatile private var databaseInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return databaseInstance
                ?: synchronized(this) {
                    Log.d(LOG_TAG, "Creating a new database instance")
                    val instance =
                        Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build()
                    databaseInstance = instance
                    Log.d(LOG_TAG, "Getting the database instance")
                    instance
                }
        }
    }
}
