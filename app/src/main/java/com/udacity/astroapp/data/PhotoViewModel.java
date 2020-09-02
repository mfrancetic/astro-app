package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.udacity.astroapp.models.Photo;

import java.util.List;

public class PhotoViewModel extends ViewModel {

    private final LiveData<List<Photo>> photos;

    /* Constructor which initializes the list of photos and receives the database */
    PhotoViewModel(@NonNull AppDatabase appDatabase) {
        photos = appDatabase.astroDao().loadAllPhotos();
    }

    public LiveData<List<Photo>> getPhotos() {
        return photos;
    }
}