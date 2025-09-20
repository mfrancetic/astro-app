package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ObservatoryRepository(private val dao: AstroDao, private val queryUtils: QueryUtils) {

    fun getAllObservatories(): Flow<List<Observatory>> = dao.loadAllObservatories()

    suspend fun getObservatoryById(id: String): Observatory? = dao.getObservatoryById(id)

    suspend fun refreshObservatories(
        latitude: Double? = null,
        longitude: Double? = null,
        forceRefresh: Boolean = false
    ): List<Observatory> {
        return withContext(Dispatchers.IO) {
            val cachedObservatories =
                dao.getObservatoriesNewerThan(
                    System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS
                )

            if (!forceRefresh && cachedObservatories.isNotEmpty()) {
                cachedObservatories
            } else {
                try {
                    val networkObservatories =
                        if (latitude != null && longitude != null) {
                            queryUtils.fetchObservatoriesFromNetwork(latitude, longitude)
                        } else {
                            queryUtils.fetchObservatoriesFromNetwork()
                        }

                    val observatoriesWithTimestamp =
                        networkObservatories.map {
                            it.copy(cacheTimestamp = System.currentTimeMillis())
                        }

                    dao.insertObservatories(observatoriesWithTimestamp)
                    observatoriesWithTimestamp
                } catch (e: Exception) {
                    cachedObservatories
                }
            }
        }
    }

    suspend fun searchObservatories(query: String): List<Observatory> {
        return withContext(Dispatchers.IO) {
            try {
                val networkObservatories = queryUtils.searchObservatoriesFromNetwork(query)
                val observatoriesWithTimestamp =
                    networkObservatories.map {
                        it.copy(cacheTimestamp = System.currentTimeMillis())
                    }
                dao.insertObservatories(observatoriesWithTimestamp)
                observatoriesWithTimestamp
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getObservatoriesNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 50.0
    ): List<Observatory> {
        return withContext(Dispatchers.IO) {
            // This would ideally be done with a spatial query in the database
            // For now, filter in memory (not efficient for large datasets)
            val allObservatories = getAllObservatories()
            // Would need proper implementation with Flow transformation
            emptyList()
        }
    }

    suspend fun getObservatoryCount(): Int = dao.getObservatoryCount()

    suspend fun clearCache() {
        dao.deleteAllObservatories()
    }
}
