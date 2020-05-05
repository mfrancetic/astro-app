package com.udacity.astroapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.models.EarthPhoto;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.models.Photo;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@SuppressWarnings("ALL")
@Dao
public interface AstroDao {

    @Query("SELECT * FROM photo")
    LiveData<List<Photo>> loadAllPhotos();

    @Query("SELECT * FROM asteroid ORDER BY asteroidId")
    LiveData<List<Asteroid>> loadAllAsteroids();

    @Query("SELECT * FROM observatory ORDER BY observatoryId")
    LiveData<List<Observatory>> loadAllObservatories();

    @Query("SELECT * FROM earthphoto")
    LiveData<List<EarthPhoto>> loadAllEarthPhotos();

    @Query("SELECT * FROM observatory WHERE observatoryId = :observatoryId")
    LiveData<Observatory> loadObservatoryById(String observatoryId);

    @Query("DELETE FROM photo")
    void deleteAllPhotos();

    @Query("DELETE FROM asteroid")
    void deleteAllAsteroids();

    @Query("DELETE FROM observatory")
    void deleteAllObservatories();

    @Query("DELETE FROM earthphoto")
    void deleteAllEarthPhotos();

    @Query("DELETE FROM observatory WHERE observatoryId = :observatoryId")
    void deleteObservatory(String observatoryId);

    @SuppressWarnings("unused")
    @Query("SELECT COUNT (*) FROM asteroid")
    int getAsteroidCount();

    @Query("SELECT COUNT (*) FROM observatory")
    int getObservatoryCount();

    @Insert(onConflict = REPLACE)
    void addPhoto(Photo photo);

    @Insert(onConflict = REPLACE)
    void addAllEarthPhotos(List<EarthPhoto> earthPhoto);

    @Insert(onConflict = REPLACE)
    void addAllObservatories(List<Observatory> observatories);

    @Insert(onConflict = REPLACE)
    void addObservatory(Observatory observatory);

    @Insert(onConflict = REPLACE)
    void addAllAsteroids(List<Asteroid> asteroids);
}