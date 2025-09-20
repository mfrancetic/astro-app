package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.EarthPhoto
import kotlinx.coroutines.flow.Flow

class EarthPhotoRepository(private val astroDao: AstroDao) {

    fun loadAllEarthPhotos(): Flow<List<EarthPhoto>> {
        return astroDao.loadAllEarthPhotos()
    }

    fun deleteAllEarthPhotos() {
        astroDao.deleteAllEarthPhotos()
    }

    fun addAllEarthPhotos(earthPhotos: List<EarthPhoto>) {
        astroDao.addAllEarthPhotos(earthPhotos)
    }
}