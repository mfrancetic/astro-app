package com.udacity.astroapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

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