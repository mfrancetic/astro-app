package com.udacity.astroapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.models.Photo;

import java.util.List;

@Dao
public interface AstroDao {

    @Query("SELECT * FROM photo ORDER BY photoId")
    LiveData<List<Photo>> loadAllPhotos();

    @Query("SELECT * FROM asteroid ORDER BY asteroidId")
    LiveData<List<Asteroid>> loadAllAsteroids();

    @Query("SELECT * FROM observatory ORDER BY observatoryId")
    LiveData<List<Observatory>> loadAllObservatories();

    @Query("SELECT * FROM photo WHERE photoId = :photoId")
    LiveData<Photo> loadPhotoById(int photoId);

    @Query("SELECT * FROM asteroid WHERE asteroidId = :asteroidId")
    LiveData<Asteroid> loadAsteroidById(int asteroidId);

    @Query("SELECT * FROM observatory WHERE observatoryId = :observatoryId")
    LiveData<Observatory> loadObservatoryById(int observatoryId);

    @Query("SELECT COUNT (*) FROM photo")
    int getPhotoCount();

    @Query("SELECT COUNT (*) FROM asteroid")
    int getAsteroidCount();

    @Query("SELECT COUNT (*) FROM observatory")
    int getObservatoryCount();

}
