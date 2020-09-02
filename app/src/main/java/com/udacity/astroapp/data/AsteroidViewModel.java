package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.udacity.astroapp.models.Asteroid;

import java.util.List;

public class AsteroidViewModel extends ViewModel {

    private final LiveData<List<Asteroid>> asteroids;

    /* Constructor which initializes the list of asteroids and receives the database */
    AsteroidViewModel(@NonNull AppDatabase appDatabase) {
        asteroids = appDatabase.astroDao().loadAllAsteroids();
    }

    public LiveData<List<Asteroid>> getAsteroids() {
        return asteroids;
    }
}