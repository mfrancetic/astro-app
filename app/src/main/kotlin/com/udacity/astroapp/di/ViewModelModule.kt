package com.udacity.astroapp.di

import com.udacity.astroapp.ui.screens.asteroid.AsteroidViewModel
import com.udacity.astroapp.ui.screens.earthphoto.EarthPhotoViewModel
import com.udacity.astroapp.ui.screens.marsphoto.MarsPhotoViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryDetailViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryViewModel
import com.udacity.astroapp.ui.screens.photo.PhotoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // Photo ViewModels
    viewModel { PhotoViewModel(get()) }

    // Asteroid ViewModels
    viewModel { AsteroidViewModel(get()) }

    // Earth Photo ViewModels
    viewModel { EarthPhotoViewModel(get()) }

    // Mars Photo ViewModels
    viewModel { MarsPhotoViewModel(get()) }

    // Observatory ViewModels
    viewModel { ObservatoryViewModel(get()) }
    viewModel { (observatoryId: String) ->
        ObservatoryDetailViewModel(get(), observatoryId)
    }
}