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
import kotlinx.coroutines.flow.Flow

@Dao
interface AstroDao {

    // Photo queries with Flow for reactive programming
    @Query("SELECT * FROM photo ORDER BY photoDate DESC")
    fun loadAllPhotos(): Flow<List<Photo>>
    
    @Query("SELECT * FROM photo WHERE photoDate = :date")
    suspend fun getPhotoByDate(date: String): Photo?
    
    @Query("SELECT * FROM photo WHERE cacheTimestamp > :timestamp")
    suspend fun getPhotosNewerThan(timestamp: Long): List<Photo>

    // Asteroid queries
    @Query("SELECT * FROM asteroid ORDER BY asteroidApproachDate ASC")
    fun loadAllAsteroids(): Flow<List<Asteroid>>
    
    @Query("SELECT * FROM asteroid WHERE cacheTimestamp > :timestamp")
    suspend fun getAsteroidsNewerThan(timestamp: Long): List<Asteroid>

    // Observatory queries  
    @Query("SELECT * FROM observatory ORDER BY observatoryName")
    fun loadAllObservatories(): Flow<List<Observatory>>
    
    @Query("SELECT * FROM observatory WHERE observatoryId = :observatoryId")
    suspend fun getObservatoryById(observatoryId: String): Observatory?
    
    @Query("SELECT * FROM observatory WHERE cacheTimestamp > :timestamp")
    suspend fun getObservatoriesNewerThan(timestamp: Long): List<Observatory>

    // Earth photo queries
    @Query("SELECT * FROM earthphoto ORDER BY earthPhotoDateTime DESC")
    fun loadAllEarthPhotos(): Flow<List<EarthPhoto>>
    
    @Query("SELECT * FROM earthphoto WHERE earthPhotoDateTime LIKE :date || '%'")
    suspend fun getEarthPhotosByDate(date: String): List<EarthPhoto>
    
    @Query("SELECT * FROM earthphoto WHERE cacheTimestamp > :timestamp")
    suspend fun getEarthPhotosNewerThan(timestamp: Long): List<EarthPhoto>

    // Mars photo queries
    @Query("SELECT * FROM marsphoto ORDER BY earthDate DESC")
    fun loadAllMarsPhotos(): Flow<List<MarsPhoto>>
    
    @Query("SELECT * FROM marsphoto WHERE sol = :sol")
    suspend fun getMarsPhotosBySol(sol: String): List<MarsPhoto>
    
    @Query("SELECT * FROM marsphoto WHERE cacheTimestamp > :timestamp")
    suspend fun getMarsPhotosNewerThan(timestamp: Long): List<MarsPhoto>

    // Delete queries
    @Query("DELETE FROM photo")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM asteroid")
    suspend fun deleteAllAsteroids()
    
    @Query("DELETE FROM asteroid WHERE cacheTimestamp < :timestamp")
    suspend fun deleteOldAsteroids(timestamp: Long)

    @Query("DELETE FROM observatory")
    suspend fun deleteAllObservatories()
    
    @Query("DELETE FROM observatory WHERE observatoryId = :observatoryId")
    suspend fun deleteObservatory(observatoryId: String)

    @Query("DELETE FROM earthphoto")
    suspend fun deleteAllEarthPhotos()
    
    @Query("DELETE FROM earthphoto WHERE cacheTimestamp < :timestamp")
    suspend fun deleteOldEarthPhotos(timestamp: Long)

    @Query("DELETE FROM marsphoto")
    suspend fun deleteAllMarsPhotos()
    
    @Query("DELETE FROM marsphoto WHERE cacheTimestamp < :timestamp")
    suspend fun deleteOldMarsPhotos(timestamp: Long)

    // Count queries
    @Query("SELECT COUNT(*) FROM asteroid")
    suspend fun getAsteroidCount(): Int

    @Query("SELECT COUNT(*) FROM observatory") 
    suspend fun getObservatoryCount(): Int

    // Insert queries
    @Insert(onConflict = REPLACE)
    suspend fun insertPhoto(photo: Photo)
    
    @Insert(onConflict = REPLACE)
    suspend fun insertPhotos(photos: List<Photo>)

    @Insert(onConflict = REPLACE)
    suspend fun insertEarthPhotos(earthPhotos: List<EarthPhoto>)

    @Insert(onConflict = REPLACE)
    suspend fun insertObservatories(observatories: List<Observatory>)

    @Insert(onConflict = REPLACE)
    suspend fun insertObservatory(observatory: Observatory)

    @Insert(onConflict = REPLACE)
    suspend fun insertAsteroids(asteroids: List<Asteroid>)

    @Insert(onConflict = REPLACE)
    suspend fun insertMarsPhotos(marsPhotos: List<MarsPhoto>)
}