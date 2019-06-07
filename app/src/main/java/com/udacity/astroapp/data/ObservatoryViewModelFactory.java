package com.udacity.astroapp.data;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ObservatoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application application;

    public ObservatoryViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ObservatoryViewModel(application);
    }
}
