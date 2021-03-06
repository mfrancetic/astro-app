package com.udacity.astroapp.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;
import android.util.Log;

import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.models.EarthPhoto;
import com.udacity.astroapp.models.MarsPhoto;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.Converters;

@Database(entities = {Photo.class, Asteroid.class, Observatory.class, EarthPhoto.class, MarsPhoto.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "astroAppDatabase";
    private static AppDatabase databaseInstance;

    /* Gets the instance of the AppDatabase and builds the database using the
     * Room.databaseBuilder method */
    public static AppDatabase getInstance(Context context) {
        if (databaseInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating a new database instance");
                databaseInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return databaseInstance;
    }

    public abstract AstroDao astroDao();
}