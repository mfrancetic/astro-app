package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.udacity.astroapp.models.Observatory;

import java.util.List;

public class ObservatoryViewModel extends ViewModel {

    private final LiveData<List<Observatory>> observatories;

    public ObservatoryViewModel(@NonNull AppDatabase appDatabase) {
//        super(application);
//        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        observatories = appDatabase.astroDao().loadAllObservatories();
    }

    public LiveData<List<Observatory>> getObservatories() {
        return observatories;
    }
}