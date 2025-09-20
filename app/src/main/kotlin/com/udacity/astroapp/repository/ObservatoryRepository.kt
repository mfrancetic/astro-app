package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Observatory
import kotlinx.coroutines.flow.Flow

class ObservatoryRepository(private val astroDao: AstroDao) {

    fun loadAllObservatories(): Flow<List<Observatory>> {
        return astroDao.loadAllObservatories()
    }

    fun loadObservatoryById(observatoryId: String): Flow<Observatory> {
        return astroDao.loadObservatoryById(observatoryId)
    }

    fun deleteAllObservatories() {
        astroDao.deleteAllObservatories()
    }

    fun deleteObservatory(observatoryId: String) {
        astroDao.deleteObservatory(observatoryId)
    }

    fun addAllObservatories(observatories: List<Observatory>) {
        astroDao.addAllObservatories(observatories)
    }

    fun addObservatory(observatory: Observatory) {
        astroDao.addObservatory(observatory)
    }

    fun getObservatoryCount(): Int {
        return astroDao.getObservatoryCount()
    }
}
