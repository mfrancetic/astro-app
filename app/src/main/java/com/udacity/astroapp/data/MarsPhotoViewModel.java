package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.udacity.astroapp.models.EarthPhoto;
import com.udacity.astroapp.models.MarsPhotoObject;

import java.util.List;

public class MarsPhotoViewModel extends ViewModel {

    private final LiveData<List<MarsPhotoObject.MarsPhoto>> marsPhotos;

    /* Constructor which initializes the list of MarsPhotos and receives the database */
    MarsPhotoViewModel(@NonNull AppDatabase appDatabase) {
        marsPhotos = appDatabase.astroDao().loadAllMarsPhotos();
    }

    public LiveData<List<MarsPhotoObject.MarsPhoto>> getMarsPhotos() {
        return marsPhotos;
    }
}