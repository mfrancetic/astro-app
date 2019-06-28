package com.udacity.astroapp.data;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ObservatoryDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase appDatabase;
    private final String observatoryId;

    /* Initializes the member variable database and id of the observatory in the constructor */
    public ObservatoryDetailViewModelFactory(AppDatabase appDatabase, String id) {
        this.appDatabase = appDatabase;
        this.observatoryId = id;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ObservatoryDetailViewModel(appDatabase, observatoryId);
    }
}