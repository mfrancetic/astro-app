package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AsteroidRepository(private val dao: AstroDao, private val queryUtils: QueryUtils) {

    fun getAllAsteroids(): Flow<List<Asteroid>> = dao.loadAllAsteroids()

    suspend fun refreshAsteroids(forceRefresh: Boolean = false): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            val cachedAsteroids =
                dao.getAsteroidsNewerThan(
                    System.currentTimeMillis() - Constants.ASTEROID_CACHE_DURATION_MILLIS
                )

            if (!forceRefresh && cachedAsteroids.isNotEmpty()) {
                cachedAsteroids
            } else {
                try {
                    val networkAsteroids = queryUtils.fetchAsteroidsFromNetwork()
                    val asteroidsWithTimestamp =
                        networkAsteroids.map {
                            it.copy(cacheTimestamp = System.currentTimeMillis())
                        }

                    // Clear old asteroids and insert new ones
                    dao.deleteAllAsteroids()
                    dao.insertAsteroids(asteroidsWithTimestamp)
                    asteroidsWithTimestamp
                } catch (e: Exception) {
                    cachedAsteroids
                }
            }
        }
    }

    suspend fun getFilteredAsteroids(isHazardous: Boolean? = null): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            try {
                // Get fresh cached asteroids first
                val cachedAsteroids =
                    dao.getAsteroidsNewerThan(
                        System.currentTimeMillis() - Constants.ASTEROID_CACHE_DURATION_MILLIS
                    )

                // If no cache, refresh first
                val asteroids =
                    if (cachedAsteroids.isEmpty()) {
                        refreshAsteroids()
                    } else {
                        cachedAsteroids
                    }

                // Apply filtering
                when (isHazardous) {
                    true -> asteroids.filter { it.asteroidIsHazardous }
                    false -> asteroids.filter { !it.asteroidIsHazardous }
                    null -> asteroids
                }
            } catch (e: Exception) {
                // Fallback to any cached data
                val allCached = dao.loadAllAsteroids().first()
                when (isHazardous) {
                    true -> allCached.filter { it.asteroidIsHazardous }
                    false -> allCached.filter { !it.asteroidIsHazardous }
                    null -> allCached
                }
            }
        }
    }

    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val expiredTimestamp =
                System.currentTimeMillis() - Constants.ASTEROID_CACHE_DURATION_MILLIS
            dao.deleteOldAsteroids(expiredTimestamp)
        }
    }

    suspend fun getAsteroidCount(): Int = dao.getAsteroidCount()

    suspend fun fetchAsteroidsForDate(date: String): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            try {
                val networkAsteroids =
                    queryUtils.fetchAsteroidsFromNetwork(startDate = date, endDate = date)
                val asteroidsWithTimestamp =
                    networkAsteroids.map { it.copy(cacheTimestamp = System.currentTimeMillis()) }

                dao.insertAsteroids(asteroidsWithTimestamp)
                asteroidsWithTimestamp
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
