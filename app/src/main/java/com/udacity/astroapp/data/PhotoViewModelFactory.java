package com.udacity.astroapp.data;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PhotoViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase appDatabase;

    /* Initializes the member variable database in the constructor */
    public PhotoViewModelFactory(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PhotoViewModel(appDatabase);
    }
}