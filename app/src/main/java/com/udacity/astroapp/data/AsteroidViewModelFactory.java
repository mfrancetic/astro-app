package com.udacity.astroapp.data;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class AsteroidViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase appDatabase;

    /* Initializes the member variable database in the constructor */
    public AsteroidViewModelFactory(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AsteroidViewModel(appDatabase);
    }
}