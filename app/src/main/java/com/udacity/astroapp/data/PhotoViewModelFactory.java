package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class PhotoViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase appDatabase;
//    private final int photoId;

    public PhotoViewModelFactory(AppDatabase appDatabase) {
//        super(application);
        this.appDatabase = appDatabase;
//        this.photoId = photoId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PhotoViewModel(appDatabase);
    }
}
