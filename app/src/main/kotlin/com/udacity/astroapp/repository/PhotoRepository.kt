package com.udacity.astroapp.repository

import android.util.Log
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PhotoRepository(private val dao: AstroDao, private val queryUtils: QueryUtils) {

    fun getAllPhotos(): Flow<List<Photo>> = dao.loadAllPhotos()

    suspend fun getPhotoByDate(date: String, forceRefresh: Boolean = false): Photo? {
        return withContext(Dispatchers.IO) {
            val cachedPhoto = dao.getPhotoByDate(date)

            if (!forceRefresh && cachedPhoto != null && isCacheValid(cachedPhoto.cacheTimestamp)) {
                cachedPhoto
            } else {
                try {
                    val networkPhoto = queryUtils.fetchPhotoFromNetwork(date)
                    networkPhoto?.let {
                        dao.insertPhoto(it.copy(cacheTimestamp = System.currentTimeMillis()))
                        it
                    } ?: cachedPhoto
                } catch (e: Exception) {
                    cachedPhoto
                }
            }
        }
    }

    suspend fun refreshPhotos() {
        withContext(Dispatchers.IO) {
            try {
                val currentPhotos = dao.loadAllPhotos().first()
                currentPhotos.forEach { photo ->
                    val refreshedPhoto = queryUtils.fetchPhotoFromNetwork(photo.photoDate)
                    refreshedPhoto?.let {
                        dao.insertPhoto(it.copy(cacheTimestamp = System.currentTimeMillis()))
                    }
                }
            } catch (e: Exception) {
                Log.e("PhotoRepository", "Error refreshing photos", e)
                // Could emit error to a flow for UI to show error state
                // For now, just log and continue - cached data will be shown
            }
        }
    }

    suspend fun clearCache() {
        dao.deleteAllPhotos()
    }

    private fun isCacheValid(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp < Constants.CACHE_DURATION_MILLIS
    }
}
