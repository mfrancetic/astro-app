package com.udacity.astroapp.di

import com.udacity.astroapp.ui.screens.asteroid.AsteroidViewModel
import com.udacity.astroapp.ui.screens.earth.EarthPhotoViewModel
import com.udacity.astroapp.ui.screens.mars.MarsPhotoViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryDetailViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryListViewModel
import com.udacity.astroapp.ui.screens.photo.PhotoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val viewModelModule = module {
    
    viewModel {
        PhotoViewModel(
            photoRepository = get()
        )
    }
    
    viewModel {
        AsteroidViewModel(
            asteroidRepository = get()
        )
    }
    
    viewModel {
        EarthPhotoViewModel(
            earthPhotoRepository = get()
        )
    }
    
    viewModel {
        MarsPhotoViewModel(
            marsPhotoRepository = get()
        )
    }
    
    viewModel {
        ObservatoryListViewModel(
            observatoryRepository = get()
        )
    }
    
    viewModel { (observatoryId: String) ->
        ObservatoryDetailViewModel(
            observatoryRepository = get(),
            observatoryId = observatoryId
        )
    }
}