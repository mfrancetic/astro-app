package com.udacity.astroapp.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.udacity.astroapp.models.Observatory;

public class ObservatoryDetailViewModel extends ViewModel {

    private final LiveData<Observatory> observatory;

    /* Constructor which initializes an observatory and receives the database and observatoryId */
    ObservatoryDetailViewModel(@NonNull AppDatabase appDatabase, String observatoryId) {
        observatory = appDatabase.astroDao().loadObservatoryById(observatoryId);
    }

    public LiveData<Observatory> getObservatory() {
        return observatory;
    }
}