package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EarthPhotoRepository(private val dao: AstroDao, private val queryUtils: QueryUtils) {

    fun getAllEarthPhotos(): Flow<List<EarthPhoto>> = dao.loadAllEarthPhotos()

    suspend fun getEarthPhotosByDate(
        date: String,
        forceRefresh: Boolean = false
    ): List<EarthPhoto> {
        return withContext(Dispatchers.IO) {
            val cachedPhotos = dao.getEarthPhotosByDate(date)

            if (
                !forceRefresh &&
                    cachedPhotos.isNotEmpty() &&
                    isCacheValid(cachedPhotos.first().cacheTimestamp)
            ) {
                cachedPhotos
            } else {
                try {
                    val networkPhotos = queryUtils.fetchEarthPhotosFromNetwork(date)
                    val photosWithTimestamp =
                        networkPhotos.map { it.copy(cacheTimestamp = System.currentTimeMillis()) }
                    dao.insertEarthPhotos(photosWithTimestamp)
                    photosWithTimestamp
                } catch (e: Exception) {
                    cachedPhotos
                }
            }
        }
    }

    suspend fun refreshEarthPhotos(dates: List<String> = emptyList()): List<EarthPhoto> {
        return withContext(Dispatchers.IO) {
            try {
                val photos = mutableListOf<EarthPhoto>()
                if (dates.isNotEmpty()) {
                    dates.forEach { date ->
                        val networkPhotos = queryUtils.fetchEarthPhotosFromNetwork(date)
                        val photosWithTimestamp =
                            networkPhotos.map {
                                it.copy(cacheTimestamp = System.currentTimeMillis())
                            }
                        photos.addAll(photosWithTimestamp)
                    }
                    dao.insertEarthPhotos(photos)
                }
                photos
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val expiredTimestamp =
                System.currentTimeMillis() - Constants.EARTH_PHOTO_CACHE_DURATION_MILLIS
            dao.deleteOldEarthPhotos(expiredTimestamp)
        }
    }

    private fun isCacheValid(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp < Constants.EARTH_PHOTO_CACHE_DURATION_MILLIS
    }
}
