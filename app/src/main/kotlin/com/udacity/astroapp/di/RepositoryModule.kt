package com.udacity.astroapp.di

import com.udacity.astroapp.repository.AsteroidRepository
import com.udacity.astroapp.repository.EarthPhotoRepository
import com.udacity.astroapp.repository.MarsPhotoRepository
import com.udacity.astroapp.repository.ObservatoryRepository
import com.udacity.astroapp.repository.PhotoRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { PhotoRepository(get()) }
    single { AsteroidRepository(get()) }
    single { EarthPhotoRepository(get()) }
    single { MarsPhotoRepository(get()) }
    single { ObservatoryRepository(get()) }
}
