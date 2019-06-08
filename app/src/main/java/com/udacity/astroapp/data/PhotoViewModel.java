package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.astroapp.models.Photo;

import java.util.List;

public class PhotoViewModel  extends ViewModel {

    private static final String LOG_TAG = PhotoViewModel.class.getSimpleName();
//    private final LiveData<List<Photo>> photos;

    private final LiveData<List<Photo>> photos;

    public PhotoViewModel(@NonNull AppDatabase appDatabase) {

//    public PhotoViewModel(@NonNull AppDatabase appDatabase, int photoId) {
//        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
//        Log.d(LOG_TAG, "Actively retrieving the tasks from the Database");
//        photos = appDatabase.astroDao().loadAllPhotos();
//        photo = appDatabase.astroDao().loadPhotoById(photoId);
        photos = appDatabase.astroDao().loadAllPhotos();

    }


    public LiveData<List<Photo>> getPhotos() {
        return photos;
    }

//    public LiveData<Photo> getPhoto() {
//        return photo;
//    }
}
