package com.udacity.astroapp.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.models.Photo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AstroDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AstroDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        dao = database.astroDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetPhoto() = runTest {
        // Given
        val photo = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo",
            photoDescription = "Test description",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image",
            cacheTimestamp = System.currentTimeMillis()
        )

        // When
        dao.insertPhoto(photo)
        val retrieved = dao.getPhotoByDate("2024-01-01")

        // Then
        assertEquals(photo, retrieved)
    }

    @Test
    fun getPhotoByDate_nonExistent_returnsNull() = runTest {
        // When
        val result = dao.getPhotoByDate("2024-12-31")

        // Then
        assertNull(result)
    }

    @Test
    fun getAllPhotos_returnsFlow() = runTest {
        // Given
        val photos = listOf(
            Photo(photoId = "1", photoDate = "2024-01-01"),
            Photo(photoId = "2", photoDate = "2024-01-02")
        )

        // When
        photos.forEach { dao.insertPhoto(it) }
        val result = dao.getAllPhotos().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].photoId)
        assertEquals("2", result[1].photoId)
    }

    @Test
    fun insertAndGetAsteroid() = runTest {
        // Given
        val asteroid = Asteroid(
            asteroidId = "test_id",
            asteroidName = "Test Asteroid",
            asteroidApproachDate = "2024-01-01",
            asteroidIsPotentiallyDangerous = true,
            asteroidVelocity = 50000.0,
            asteroidDistanceFromEarth = 1000000.0,
            asteroidMinDiameter = 1.0,
            asteroidMaxDiameter = 2.0,
            asteroidAbsoluteMagnitude = 18.5,
            cacheTimestamp = System.currentTimeMillis()
        )

        // When
        dao.insertAsteroid(asteroid)
        val result = dao.getAllAsteroids().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(asteroid, result[0])
    }

    @Test
    fun getAsteroidsNewerThan_filtersCorrectly() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val oldAsteroid = Asteroid(
            asteroidId = "old",
            asteroidName = "Old Asteroid",
            asteroidApproachDate = "2024-01-01",
            cacheTimestamp = currentTime - 10000
        )
        val newAsteroid = Asteroid(
            asteroidId = "new",
            asteroidName = "New Asteroid",
            asteroidApproachDate = "2024-01-02",
            cacheTimestamp = currentTime - 1000
        )

        // When
        dao.insertAsteroid(oldAsteroid)
        dao.insertAsteroid(newAsteroid)
        val result = dao.getAsteroidsNewerThan(currentTime - 5000)

        // Then
        assertEquals(1, result.size)
        assertEquals("new", result[0].asteroidId)
    }

    @Test
    fun insertAndGetObservatory() = runTest {
        // Given
        val observatory = Observatory(
            observatoryId = "test_id",
            observatoryName = "Test Observatory",
            observatoryAddress = "123 Test Street",
            observatoryLatitude = 40.7128,
            observatoryLongitude = -74.0060,
            observatoryOpenNow = true,
            cacheTimestamp = System.currentTimeMillis()
        )

        // When
        dao.insertObservatory(observatory)
        val retrieved = dao.getObservatoryById("test_id")

        // Then
        assertEquals(observatory, retrieved)
    }

    @Test
    fun getAllObservatories_returnsFlow() = runTest {
        // Given
        val observatories = listOf(
            Observatory(
                observatoryId = "1",
                observatoryName = "Observatory 1",
                observatoryLatitude = 40.7128,
                observatoryLongitude = -74.0060
            ),
            Observatory(
                observatoryId = "2",
                observatoryName = "Observatory 2",
                observatoryLatitude = 34.0522,
                observatoryLongitude = -118.2437
            )
        )

        // When
        observatories.forEach { dao.insertObservatory(it) }
        val result = dao.loadAllObservatories().first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun deleteAllPhotos_clearsPhotos() = runTest {
        // Given
        val photo = Photo(photoId = "test", photoDate = "2024-01-01")
        dao.insertPhoto(photo)

        // When
        dao.deleteAllPhotos()
        val result = dao.getAllPhotos().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun deleteAllAsteroids_clearsAsteroids() = runTest {
        // Given
        val asteroid = Asteroid(asteroidId = "test", asteroidName = "Test", asteroidApproachDate = "2024-01-01")
        dao.insertAsteroid(asteroid)

        // When
        dao.deleteAllAsteroids()
        val result = dao.getAllAsteroids().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun deleteAllObservatories_clearsObservatories() = runTest {
        // Given
        val observatory = Observatory(
            observatoryId = "test",
            observatoryName = "Test Observatory",
            observatoryLatitude = 40.0,
            observatoryLongitude = -74.0
        )
        dao.insertObservatory(observatory)

        // When
        dao.deleteAllObservatories()
        val result = dao.loadAllObservatories().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun getPhotoCount_returnsCorrectCount() = runTest {
        // Given
        val photos = (1..5).map { 
            Photo(photoId = "test_$it", photoDate = "2024-01-0$it")
        }
        photos.forEach { dao.insertPhoto(it) }

        // When
        val count = dao.getPhotoCount()

        // Then
        assertEquals(5, count)
    }

    @Test
    fun getAsteroidCount_returnsCorrectCount() = runTest {
        // Given
        val asteroids = (1..3).map {
            Asteroid(asteroidId = "test_$it", asteroidName = "Asteroid $it", asteroidApproachDate = "2024-01-0$it")
        }
        asteroids.forEach { dao.insertAsteroid(it) }

        // When
        val count = dao.getAsteroidCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun getObservatoryCount_returnsCorrectCount() = runTest {
        // Given
        val observatories = (1..4).map {
            Observatory(
                observatoryId = "test_$it",
                observatoryName = "Observatory $it",
                observatoryLatitude = 40.0 + it,
                observatoryLongitude = -74.0 - it
            )
        }
        observatories.forEach { dao.insertObservatory(it) }

        // When
        val count = dao.getObservatoryCount()

        // Then
        assertEquals(4, count)
    }
}