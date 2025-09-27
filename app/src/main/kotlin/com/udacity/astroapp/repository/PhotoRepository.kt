package com.udacity.astroapp.repository

import android.util.Log
import com.udacity.astroapp.cache.CacheManager
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.repository.base.BaseCachedRepository
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhotoRepository(
    private val dao: AstroDao,
    private val queryUtils: QueryUtils,
    cacheManager: CacheManager
) : BaseCachedRepository(cacheManager) {

    fun getAllPhotos(): Flow<List<Photo>> = dao.loadAllPhotos()

    suspend fun getPhotoByDate(date: String, forceRefresh: Boolean = false): Photo? {
        return handleCacheOperation(
            repositoryName = "PhotoRepository",
            operation = "getPhotoByDate($date)",
            cacheOperation = {
                val cachedPhoto = dao.getPhotoByDate(date)
                if (
                    !forceRefresh &&
                        cachedPhoto != null &&
                        isCacheValid(
                            cachedPhoto.cacheTimestamp,
                            Constants.PHOTO_CACHE_DURATION_MILLIS
                        )
                ) {
                    cachedPhoto
                } else {
                    null
                }
            },
            networkOperation = {
                val networkPhoto = queryUtils.fetchPhotoFromNetwork(date)
                networkPhoto?.let {
                    dao.insertPhoto(it.copy(cacheTimestamp = getCurrentTimestamp()))
                    it
                }
            },
            fallbackOperation = { dao.getPhotoByDate(date) }
        )
    }

    suspend fun refreshPhotos() {
        try {
            val currentPhotos = dao.loadAllPhotos().first()
            currentPhotos.forEach { photo ->
                val refreshedPhoto = queryUtils.fetchPhotoFromNetwork(photo.photoDate)
                refreshedPhoto?.let {
                    dao.insertPhoto(it.copy(cacheTimestamp = getCurrentTimestamp()))
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error refreshing photos", e)
            // Could emit error to a flow for UI to show error state
            // For now, just log and continue - cached data will be shown
        }
    }

    suspend fun clearCache() {
        dao.deleteAllPhotos()
    }
}
