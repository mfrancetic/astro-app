package com.udacity.astroapp.repository

import androidx.lifecycle.LiveData
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.EarthPhoto

class EarthPhotoRepository(private val astroDao: AstroDao) {

    fun loadAllEarthPhotos(): LiveData<List<EarthPhoto>> {
        return astroDao.loadAllEarthPhotos()
    }

    fun deleteAllEarthPhotos() {
        astroDao.deleteAllEarthPhotos()
    }

    fun addAllEarthPhotos(earthPhotos: List<EarthPhoto>) {
        astroDao.addAllEarthPhotos(earthPhotos)
    }
}