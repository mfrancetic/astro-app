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
                    System.currentTimeMillis() - Constants.OBSERVATORY_CACHE_DURATION_MILLIS
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
            try {
                // First ensure we have fresh observatories for this location
                val freshObservatories = refreshObservatories(latitude, longitude)

                // Filter by distance
                freshObservatories.filter { observatory ->
                    val distance =
                        calculateDistance(
                            latitude,
                            longitude,
                            observatory.observatoryLatitude,
                            observatory.observatoryLongitude
                        )
                    distance <= radiusKm
                }
            } catch (e: Exception) {
                // Fallback to cached observatories
                val allCached =
                    dao.getObservatoriesNewerThan(
                        System.currentTimeMillis() - Constants.OBSERVATORY_CACHE_DURATION_MILLIS
                    )
                allCached.filter { observatory ->
                    val distance =
                        calculateDistance(
                            latitude,
                            longitude,
                            observatory.observatoryLatitude,
                            observatory.observatoryLongitude
                        )
                    distance <= radiusKm
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                    Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) *
                    Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    suspend fun getObservatoryCount(): Int = dao.getObservatoryCount()

    suspend fun clearCache() {
        dao.deleteAllObservatories()
    }
}
