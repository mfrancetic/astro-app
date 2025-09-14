package com.udacity.astroapp.repository

import androidx.lifecycle.LiveData
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Asteroid

class AsteroidRepository(private val astroDao: AstroDao) {

    fun loadAllAsteroids(): LiveData<List<Asteroid>> {
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