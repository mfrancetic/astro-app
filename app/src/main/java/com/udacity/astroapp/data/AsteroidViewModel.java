package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.astroapp.models.Asteroid;

import java.util.List;

public class AsteroidViewModel extends ViewModel {

    private static final String LOG_TAG = AsteroidViewModel.class.getSimpleName();

    private LiveData<List<Asteroid>> asteroids;

//    private LiveData<Asteroid> asteroid;

    public AsteroidViewModel(@NonNull AppDatabase appDatabase) {
//        super(application);
//         appDatabase = AppDatabase.getInstance(this.getApplication());
//        Log.d(LOG_TAG, "Actively retr ieving the tasks from the Database");
        asteroids = appDatabase.astroDao().loadAllAsteroids();
    }

    public LiveData<List<Asteroid>> getAsteroids() {
        return asteroids;
    }

}
