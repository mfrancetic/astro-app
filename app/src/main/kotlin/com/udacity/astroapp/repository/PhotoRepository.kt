package com.udacity.astroapp.repository

import androidx.lifecycle.LiveData
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Photo

class PhotoRepository(private val astroDao: AstroDao) {

    fun loadAllPhotos(): LiveData<List<Photo>> {
        return astroDao.loadAllPhotos()
    }

    fun deleteAllPhotos() {
        astroDao.deleteAllPhotos()
    }

    fun addPhoto(photo: Photo) {
        astroDao.addPhoto(photo)
    }
}