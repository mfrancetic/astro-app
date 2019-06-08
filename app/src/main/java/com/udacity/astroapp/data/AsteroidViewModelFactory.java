package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class AsteroidViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase appDatabase;
//    private Application application;

    public AsteroidViewModelFactory(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
//        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AsteroidViewModel(appDatabase);
    }
}
