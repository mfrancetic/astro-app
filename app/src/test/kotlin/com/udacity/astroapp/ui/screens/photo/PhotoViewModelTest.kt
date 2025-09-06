package com.udacity.astroapp.ui.screens.photo

import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.repository.PhotoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

class PhotoViewModelTest {

    private lateinit var repository: PhotoRepository
    private lateinit var viewModel: PhotoViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = PhotoViewModel(repository)
    }

    @Test
    fun `loadTodaysPhoto should load photo successfully`() = runTest {
        // Given
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo",
            photoDescription = "Test description",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        coEvery { repository.getPhotoByDate(any(), any()) } returns testPhoto

        // When & Then
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.loadPhotoByDate("2024-01-01")
            
            expectState {
                PhotoState(
                    isLoading = true,
                    selectedDate = "2024-01-01",
                    error = null
                )
            }
            
            expectState {
                PhotoState(
                    photo = testPhoto,
                    isLoading = false,
                    selectedDate = "2024-01-01",
                    error = null
                )
            }
        }
        
        coVerify { repository.getPhotoByDate("2024-01-01", false) }
    }

    @Test
    fun `loadPhotoByDate should handle error gracefully`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getPhotoByDate(any(), any()) } throws Exception(errorMessage)

        // When & Then
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.loadPhotoByDate("2024-01-01")
            
            expectState {
                PhotoState(
                    isLoading = true,
                    selectedDate = "2024-01-01",
                    error = null
                )
            }
            
            expectState {
                PhotoState(
                    isLoading = false,
                    selectedDate = "2024-01-01",
                    error = errorMessage
                )
            }
            
            expectSideEffect(PhotoSideEffect.ShowError(errorMessage))
        }
    }

    @Test
    fun `onDateSelected should load photo for selected date`() = runTest {
        // Given
        val selectedDate = "2024-02-15"
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Valentine's Photo",
            photoDate = selectedDate,
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        coEvery { repository.getPhotoByDate(selectedDate, any()) } returns testPhoto

        // When & Then
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.onDateSelected(selectedDate)
            
            // Should hide date picker first
            expectState {
                PhotoState(showDatePicker = false)
            }
            
            // Then load the photo
            expectState {
                PhotoState(
                    isLoading = true,
                    selectedDate = selectedDate,
                    showDatePicker = false
                )
            }
            
            expectState {
                PhotoState(
                    photo = testPhoto,
                    isLoading = false,
                    selectedDate = selectedDate,
                    showDatePicker = false
                )
            }
        }
    }

    @Test
    fun `onSharePhotoClicked should emit SharePhoto side effect when photo exists`() = runTest {
        // Given
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )

        // When & Then
        viewModel.test(this) {
            // Set up state with photo
            val stateWithPhoto = PhotoState(photo = testPhoto)
            expectInitialState(PhotoState())
            
            // Manually set state for testing
            containerHost.loadPhotoByDate("2024-01-01")
            coEvery { repository.getPhotoByDate(any(), any()) } returns testPhoto
            
            // Skip initial loading states and get to photo loaded state
            runOnCreate()
            
            containerHost.onSharePhotoClicked()
            expectSideEffect(PhotoSideEffect.SharePhoto(testPhoto))
        }
    }

    @Test
    fun `onDatePickerClicked should show date picker`() = runTest {
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.onDatePickerClicked()
            expectState {
                PhotoState(showDatePicker = true)
            }
        }
    }

    @Test
    fun `onDatePickerDismissed should hide date picker`() = runTest {
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.onDatePickerDismissed()
            expectState {
                PhotoState(showDatePicker = false)
            }
        }
    }

    @Test
    fun `onRefresh should reload current date with force refresh`() = runTest {
        // Given
        val currentDate = "2024-01-01"
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Refreshed Photo",
            photoDate = currentDate,
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        coEvery { repository.getPhotoByDate(any(), any()) } returns testPhoto

        // When & Then
        viewModel.test(this) {
            // First set a selected date
            val initialState = PhotoState(selectedDate = currentDate)
            expectInitialState(PhotoState())
            
            containerHost.onRefresh()
            
            expectState {
                PhotoState(
                    isLoading = true,
                    isRefreshing = true,
                    selectedDate = ""
                )
            }
        }
        
        coVerify { repository.getPhotoByDate(any(), true) }
    }

    @Test
    fun `onErrorDismissed should clear error state`() = runTest {
        viewModel.test(this) {
            expectInitialState(PhotoState())
            containerHost.onErrorDismissed()
            expectState {
                PhotoState(error = null)
            }
        }
    }
}