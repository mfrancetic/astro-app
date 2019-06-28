package com.udacity.astroapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.udacity.astroapp.models.Observatory;

import java.util.List;

public class ObservatoryViewModel extends ViewModel {

    private final LiveData<List<Observatory>> observatories;

    /* Constructor which initializes the list of observatories and receives the database */
    ObservatoryViewModel(@NonNull AppDatabase appDatabase) {
        observatories = appDatabase.astroDao().loadAllObservatories();
    }

    public LiveData<List<Observatory>> getObservatories() {
        return observatories;
    }
}