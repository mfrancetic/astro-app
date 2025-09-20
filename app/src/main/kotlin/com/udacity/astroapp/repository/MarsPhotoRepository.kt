package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.MarsPhoto
import kotlinx.coroutines.flow.Flow

class MarsPhotoRepository(private val astroDao: AstroDao) {

    fun loadAllMarsPhotos(): Flow<List<MarsPhoto>> {
        return astroDao.loadAllMarsPhotos()
    }

    fun deleteAllMarsPhotos() {
        astroDao.deleteAllMarsPhotos()
    }

    fun addAllMarsPhotos(marsPhotos: List<MarsPhoto>) {
        astroDao.addAllMarsPhotos(marsPhotos)
    }
}