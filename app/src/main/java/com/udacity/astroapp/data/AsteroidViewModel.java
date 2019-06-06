package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.astroapp.models.Asteroid;

import java.util.List;

public class AsteroidViewModel extends AndroidViewModel {

    private static final String LOG_TAG = AsteroidViewModel.class.getSimpleName();

    private LiveData<List<Asteroid>> asteroids;

    private LiveData<Asteroid> asteroid;

    public AsteroidViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        Log.d(LOG_TAG, "Actively retrieving the tasks from the Database");
        asteroids = appDatabase.astroDao().loadAllAsteroids();
    }

    LiveData<List<Asteroid>> getAsteroids() {
        return asteroids;
    }

//    public AsteroidViewModel(AppDatabase appDatabase, int asteroidId) {
//        super();
//        asteroid = appDatabase.astroDao().loadAsteroidById(asteroidId);
//    }

//    LiveData<Asteroid> getAsteroid() {
//        return asteroid;
//    }
}
