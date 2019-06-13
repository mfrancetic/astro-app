package com.udacity.astroapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.udacity.astroapp.models.Observatory;


public class ObservatoryDetailViewModel extends ViewModel {

    private final LiveData<Observatory> observatory;

    public ObservatoryDetailViewModel(@NonNull AppDatabase appDatabase, String observatoryId) {
        observatory = appDatabase.astroDao().loadObservatoryById(observatoryId);
    }

    public LiveData<Observatory> getObservatory() {
        return observatory;
    }


}
