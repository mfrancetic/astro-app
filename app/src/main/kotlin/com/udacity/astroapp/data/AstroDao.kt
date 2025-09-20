package com.udacity.astroapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.models.Photo

@Dao
interface AstroDao {

    @Query("SELECT * FROM photo")
    fun loadAllPhotos(): LiveData<List<Photo>>

    @Query("SELECT * FROM asteroid ORDER BY asteroidId")
    fun loadAllAsteroids(): LiveData<List<Asteroid>>

    @Query("SELECT * FROM observatory ORDER BY observatoryId")
    fun loadAllObservatories(): LiveData<List<Observatory>>

    @Query("SELECT * FROM earthphoto")
    fun loadAllEarthPhotos(): LiveData<List<EarthPhoto>>

    @Query("SELECT * FROM marsphoto")
    fun loadAllMarsPhotos(): LiveData<List<MarsPhoto>>

    @Query("SELECT * FROM observatory WHERE observatoryId = :observatoryId")
    fun loadObservatoryById(observatoryId: String): LiveData<Observatory>

    @Query("DELETE FROM photo")
    fun deleteAllPhotos()

    @Query("DELETE FROM asteroid")
    fun deleteAllAsteroids()

    @Query("DELETE FROM observatory")
    fun deleteAllObservatories()

    @Query("DELETE FROM earthphoto")
    fun deleteAllEarthPhotos()

    @Query("DELETE FROM marsphoto")
    fun deleteAllMarsPhotos()

    @Query("DELETE FROM observatory WHERE observatoryId = :observatoryId")
    fun deleteObservatory(observatoryId: String)

    @Query("SELECT COUNT (*) FROM asteroid")
    fun getAsteroidCount(): Int

    @Query("SELECT COUNT (*) FROM observatory")
    fun getObservatoryCount(): Int

    @Insert(onConflict = REPLACE)
    fun addPhoto(photo: Photo)

    @Insert(onConflict = REPLACE)
    fun addAllEarthPhotos(earthPhoto: List<EarthPhoto>)

    @Insert(onConflict = REPLACE)
    fun addAllObservatories(observatories: List<Observatory>)

    @Insert(onConflict = REPLACE)
    fun addObservatory(observatory: Observatory)

    @Insert(onConflict = REPLACE)
    fun addAllAsteroids(asteroids: List<Asteroid>)

    @Insert(onConflict = REPLACE)
    fun addAllMarsPhotos(marsPhotos: List<MarsPhoto>)
}