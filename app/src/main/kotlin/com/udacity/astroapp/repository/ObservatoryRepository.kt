package com.udacity.astroapp.repository

import androidx.lifecycle.LiveData
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Observatory

class ObservatoryRepository(private val astroDao: AstroDao) {

    fun loadAllObservatories(): LiveData<List<Observatory>> {
        return astroDao.loadAllObservatories()
    }

    fun loadObservatoryById(observatoryId: String): LiveData<Observatory> {
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