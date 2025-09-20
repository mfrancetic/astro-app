package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Asteroid
import kotlinx.coroutines.flow.Flow

class AsteroidRepository(private val astroDao: AstroDao) {

    fun loadAllAsteroids(): Flow<List<Asteroid>> {
        return astroDao.loadAllAsteroids()
    }

    fun deleteAllAsteroids() {
        astroDao.deleteAllAsteroids()
    }

    fun addAllAsteroids(asteroids: List<Asteroid>) {
        astroDao.addAllAsteroids(asteroids)
    }

    fun getAsteroidCount(): Int {
        return astroDao.getAsteroidCount()
    }
}