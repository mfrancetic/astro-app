package com.udacity.astroapp.data;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ObservatoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private AppDatabase appDatabase;

    /* Initializes the member variable database in the constructor */
    public ObservatoryViewModelFactory(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ObservatoryViewModel(appDatabase);
    }
}