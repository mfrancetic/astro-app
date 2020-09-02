package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.udacity.astroapp.models.MarsPhoto;

import java.util.List;

public class MarsPhotoViewModel extends ViewModel {

    private final LiveData<List<MarsPhoto>> marsPhotos;

    /* Constructor which initializes the list of MarsPhotos and receives the database */
    MarsPhotoViewModel(@NonNull AppDatabase appDatabase) {
        marsPhotos = appDatabase.astroDao().loadAllMarsPhotos();
    }

    public LiveData<List<MarsPhoto>> getMarsPhotos() {
        return marsPhotos;
    }
}