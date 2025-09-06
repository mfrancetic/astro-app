package com.udacity.astroapp.repository

import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.QueryUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PhotoRepositoryTest {

    private lateinit var dao: AstroDao
    private lateinit var queryUtils: QueryUtils
    private lateinit var repository: PhotoRepository

    @Before
    fun setup() {
        dao = mockk()
        queryUtils = mockk()
        repository = PhotoRepository(dao, queryUtils)
    }

    @Test
    fun `getPhotoByDate should return cached photo when not force refresh and cache is fresh`() = runTest {
        // Given
        val date = "2024-01-01"
        val cachedPhoto = Photo(
            photoId = "cached_id",
            photoTitle = "Cached Photo",
            photoDate = date,
            cacheTimestamp = System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS + 1000 // Fresh cache
        )
        coEvery { dao.getPhotoByDate(date) } returns cachedPhoto

        // When
        val result = repository.getPhotoByDate(date, forceRefresh = false)

        // Then
        assertEquals(cachedPhoto, result)
        coVerify { dao.getPhotoByDate(date) }
        coVerify(exactly = 0) { queryUtils.fetchPhotoFromNetwork(any()) }
    }

    @Test
    fun `getPhotoByDate should fetch from network when cache is stale`() = runTest {
        // Given
        val date = "2024-01-01"
        val stalePhoto = Photo(
            photoId = "stale_id",
            photoTitle = "Stale Photo",
            photoDate = date,
            cacheTimestamp = System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS - 1000 // Stale cache
        )
        val networkPhoto = Photo(
            photoId = "network_id",
            photoTitle = "Network Photo",
            photoDate = date,
            cacheTimestamp = System.currentTimeMillis()
        )
        
        coEvery { dao.getPhotoByDate(date) } returns stalePhoto
        coEvery { queryUtils.fetchPhotoFromNetwork(date) } returns networkPhoto
        coEvery { dao.insertPhoto(networkPhoto) } returns Unit

        // When
        val result = repository.getPhotoByDate(date, forceRefresh = false)

        // Then
        assertEquals(networkPhoto, result)
        coVerify { dao.getPhotoByDate(date) }
        coVerify { queryUtils.fetchPhotoFromNetwork(date) }
        coVerify { dao.insertPhoto(networkPhoto) }
    }

    @Test
    fun `getPhotoByDate should fetch from network when forceRefresh is true`() = runTest {
        // Given
        val date = "2024-01-01"
        val networkPhoto = Photo(
            photoId = "network_id",
            photoTitle = "Fresh Network Photo",
            photoDate = date,
            cacheTimestamp = System.currentTimeMillis()
        )
        
        coEvery { queryUtils.fetchPhotoFromNetwork(date) } returns networkPhoto
        coEvery { dao.insertPhoto(networkPhoto) } returns Unit

        // When
        val result = repository.getPhotoByDate(date, forceRefresh = true)

        // Then
        assertEquals(networkPhoto, result)
        coVerify { queryUtils.fetchPhotoFromNetwork(date) }
        coVerify { dao.insertPhoto(networkPhoto) }
        coVerify(exactly = 0) { dao.getPhotoByDate(date) }
    }

    @Test
    fun `getPhotoByDate should return cached photo when network fails`() = runTest {
        // Given
        val date = "2024-01-01"
        val cachedPhoto = Photo(
            photoId = "cached_id",
            photoTitle = "Cached Photo",
            photoDate = date,
            cacheTimestamp = System.currentTimeMillis() - Constants.CACHE_DURATION_MILLIS - 1000 // Stale but exists
        )
        
        coEvery { dao.getPhotoByDate(date) } returns cachedPhoto
        coEvery { queryUtils.fetchPhotoFromNetwork(date) } throws Exception("Network error")

        // When
        val result = repository.getPhotoByDate(date, forceRefresh = false)

        // Then
        assertEquals(cachedPhoto, result)
        coVerify { dao.getPhotoByDate(date) }
        coVerify { queryUtils.fetchPhotoFromNetwork(date) }
        coVerify(exactly = 0) { dao.insertPhoto(any()) }
    }

    @Test
    fun `getPhotoByDate should return null when no cache and network fails`() = runTest {
        // Given
        val date = "2024-01-01"
        
        coEvery { dao.getPhotoByDate(date) } returns null
        coEvery { queryUtils.fetchPhotoFromNetwork(date) } throws Exception("Network error")

        // When
        val result = repository.getPhotoByDate(date, forceRefresh = false)

        // Then
        assertEquals(null, result)
        coVerify { dao.getPhotoByDate(date) }
        coVerify { queryUtils.fetchPhotoFromNetwork(date) }
    }

    @Test
    fun `getAllPhotos should return flow from dao`() = runTest {
        // Given
        val photos = listOf(
            Photo(photoId = "1", photoDate = "2024-01-01"),
            Photo(photoId = "2", photoDate = "2024-01-02")
        )
        coEvery { dao.getAllPhotos() } returns flowOf(photos)

        // When
        val result = repository.getAllPhotos()

        // Then
        result.collect { photoList ->
            assertEquals(photos, photoList)
        }
        coVerify { dao.getAllPhotos() }
    }

    @Test
    fun `getPhotoCount should return count from dao`() = runTest {
        // Given
        val expectedCount = 42
        coEvery { dao.getPhotoCount() } returns expectedCount

        // When
        val result = repository.getPhotoCount()

        // Then
        assertEquals(expectedCount, result)
        coVerify { dao.getPhotoCount() }
    }

    @Test
    fun `clearCache should call dao deleteAllPhotos`() = runTest {
        // Given
        coEvery { dao.deleteAllPhotos() } returns Unit

        // When
        repository.clearCache()

        // Then
        coVerify { dao.deleteAllPhotos() }
    }
}