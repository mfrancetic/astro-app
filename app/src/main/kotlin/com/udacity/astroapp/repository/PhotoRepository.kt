package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Photo
import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val astroDao: AstroDao) {

    fun loadAllPhotos(): Flow<List<Photo>> {
        return astroDao.loadAllPhotos()
    }

    fun deleteAllPhotos() {
        astroDao.deleteAllPhotos()
    }

    fun addPhoto(photo: Photo) {
        astroDao.addPhoto(photo)
    }
}