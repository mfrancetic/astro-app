package com.udacity.astroapp

import android.app.Application
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.repository.*
import com.udacity.astroapp.ui.screens.asteroid.AsteroidViewModel
import com.udacity.astroapp.ui.screens.earth.EarthPhotoViewModel
import com.udacity.astroapp.ui.screens.mars.MarsPhotoViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryDetailViewModel
import com.udacity.astroapp.ui.screens.observatory.ObservatoryListViewModel
import com.udacity.astroapp.ui.screens.photo.PhotoViewModel
import com.udacity.astroapp.utils.QueryUtils
import io.mockk.mockk
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        stopKoin() // Stop any existing Koin instance
        
        startKoin {
            androidContext(this@TestApplication)
            modules(testModules)
        }
    }
}

val testModules = module {
    // Mock dependencies
    single<AstroDao> { mockk(relaxed = true) }
    single<QueryUtils> { mockk(relaxed = true) }
    
    // Mock repositories
    single<PhotoRepository> { mockk(relaxed = true) }
    single<AsteroidRepository> { mockk(relaxed = true) }
    single<EarthPhotoRepository> { mockk(relaxed = true) }
    single<MarsPhotoRepository> { mockk(relaxed = true) }
    single<ObservatoryRepository> { mockk(relaxed = true) }
    
    // ViewModels
    viewModel { PhotoViewModel(get()) }
    viewModel { AsteroidViewModel(get()) }
    viewModel { EarthPhotoViewModel(get()) }
    viewModel { MarsPhotoViewModel(get()) }
    viewModel { ObservatoryListViewModel(get()) }
    viewModel { (observatoryId: String) ->
        ObservatoryDetailViewModel(get(), observatoryId)
    }
}