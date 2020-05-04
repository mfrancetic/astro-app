package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.udacity.astroapp.models.EarthPhoto;

import java.util.List;

public class EarthPhotoViewModel extends ViewModel {

    private final LiveData<List<EarthPhoto>> earthPhotos;

    /* Constructor which initializes the list of EarthPhotos and receives the database */
    EarthPhotoViewModel(@NonNull AppDatabase appDatabase) {
        earthPhotos = appDatabase.astroDao().loadAllEarthPhotos();
    }

    public LiveData<List<EarthPhoto>> getEarthPhotos() {
        return earthPhotos;
    }
}