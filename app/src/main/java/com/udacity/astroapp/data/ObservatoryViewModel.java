package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.udacity.astroapp.models.Observatory;

import java.util.List;

public class ObservatoryViewModel extends AndroidViewModel {

    private final LiveData<List<Observatory>> observatories;

    public ObservatoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(this.getApplication());
        observatories = appDatabase.astroDao().loadAllObservatories();
    }

    public LiveData<List<Observatory>> getObservatories() {
        return observatories;
    }
}