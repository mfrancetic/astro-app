package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AsteroidRepository(
    private val dao: AstroDao,
    private val queryUtils: QueryUtils
) {

    fun getAllAsteroids(): Flow<List<Asteroid>> = dao.loadAllAsteroids()

    suspend fun refreshAsteroids(forceRefresh: Boolean = false): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            val cachedAsteroids = dao.getAsteroidsNewerThan(
                System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS
            )
            
            if (!forceRefresh && cachedAsteroids.isNotEmpty()) {
                cachedAsteroids
            } else {
                try {
                    val networkAsteroids = queryUtils.fetchAsteroidsFromNetwork()
                    val asteroidsWithTimestamp = networkAsteroids.map { 
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
            // Since Room doesn't have a direct filtering query, filter in memory for now
            // In a more complex app, you'd add specific queries to the DAO
            val allAsteroids = dao.loadAllAsteroids()
            // This would need to be implemented with proper Flow transformation
            // For now, returning empty list as placeholder
            emptyList()
        }
    }

    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val expiredTimestamp = System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS
            dao.deleteOldAsteroids(expiredTimestamp)
        }
    }

    suspend fun getAsteroidCount(): Int = dao.getAsteroidCount()
}