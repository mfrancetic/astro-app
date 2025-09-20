package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.data.api.MarsPhotoService
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MarsPhotoRepository(
    private val dao: AstroDao,
    private val marsPhotoService: MarsPhotoService
) {
    fun getAllMarsPhotos(): Flow<List<MarsPhoto>> = dao.loadAllMarsPhotos()

    suspend fun getMarsPhotosBySol(sol: String, forceRefresh: Boolean = false): List<MarsPhoto> {
        return withContext(Dispatchers.IO) {
            val cachedPhotos = dao.getMarsPhotosBySol(sol)

            if (
                !forceRefresh &&
                    cachedPhotos.isNotEmpty() &&
                    isCacheValid(cachedPhotos.first().cacheTimestamp)
            ) {
                cachedPhotos
            } else {
                try {
                    val response = marsPhotoService.getMarsPhotosBySol(sol, Constants.NASA_API_KEY)
                    if (response.isSuccessful) {
                        val networkPhotos = response.body()?.photos ?: emptyList()
                        val photosWithTimestamp =
                            networkPhotos.map {
                                it.copy(cacheTimestamp = System.currentTimeMillis())
                            }
                        dao.insertMarsPhotos(photosWithTimestamp)
                        photosWithTimestamp
                    } else {
                        cachedPhotos
                    }
                } catch (e: Exception) {
                    cachedPhotos
                }
            }
        }
    }

    suspend fun getMarsPhotosByDate(date: String, forceRefresh: Boolean = false): List<MarsPhoto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = marsPhotoService.getMarsPhotos(date, Constants.NASA_API_KEY)
                if (response.isSuccessful) {
                    val networkPhotos = response.body()?.photos ?: emptyList()
                    val photosWithTimestamp =
                        networkPhotos.map { it.copy(cacheTimestamp = System.currentTimeMillis()) }
                    dao.insertMarsPhotos(photosWithTimestamp)
                    photosWithTimestamp
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val expiredTimestamp = System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS
            dao.deleteOldMarsPhotos(expiredTimestamp)
        }
    }

    private fun isCacheValid(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp < Constants.CACHE_DURATION_MILLIS
    }
}
