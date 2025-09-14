package com.udacity.astroapp.repository

import androidx.lifecycle.LiveData
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.MarsPhoto

class MarsPhotoRepository(private val astroDao: AstroDao) {

    fun loadAllMarsPhotos(): LiveData<List<MarsPhoto>> {
        return astroDao.loadAllMarsPhotos()
    }

    fun deleteAllMarsPhotos() {
        astroDao.deleteAllMarsPhotos()
    }

    fun addAllMarsPhotos(marsPhotos: List<MarsPhoto>) {
        astroDao.addAllMarsPhotos(marsPhotos)
    }
}