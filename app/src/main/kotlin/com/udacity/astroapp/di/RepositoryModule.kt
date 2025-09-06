package com.udacity.astroapp.di

import com.udacity.astroapp.repository.AsteroidRepository
import com.udacity.astroapp.repository.EarthPhotoRepository
import com.udacity.astroapp.repository.MarsPhotoRepository
import com.udacity.astroapp.repository.ObservatoryRepository
import com.udacity.astroapp.repository.PhotoRepository
import org.koin.dsl.module

val repositoryModule = module {
    
    single {
        PhotoRepository(
            dao = get(),
            queryUtils = get()
        )
    }
    
    single {
        AsteroidRepository(
            dao = get(),
            queryUtils = get()
        )
    }
    
    single {
        EarthPhotoRepository(
            dao = get(),
            queryUtils = get()
        )
    }
    
    single {
        MarsPhotoRepository(
            dao = get(),
            marsPhotoService = get()
        )
    }
    
    single {
        ObservatoryRepository(
            dao = get(),
            queryUtils = get()
        )
    }
}