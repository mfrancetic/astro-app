package com.udacity.astroapp.ui.screens.photo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.ui.theme.AstroTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.orbitmvi.orbit.test.test

@RunWith(AndroidJUnit4::class)
class PhotoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: PhotoViewModel
    private lateinit var testStateFlow: MutableStateFlow<PhotoState>

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        testStateFlow = MutableStateFlow(PhotoState())
        
        every { mockViewModel.container.stateFlow } returns testStateFlow
        every { mockViewModel.container.sideEffectFlow } returns MutableStateFlow()
    }

    @Test
    fun photoScreen_initialLoading_showsLoadingIndicator() {
        // Given - loading state
        testStateFlow.value = PhotoState(isLoading = true)

        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun photoScreen_withPhoto_displaysPhotoContent() {
        // Given - state with photo
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo Title",
            photoDescription = "Test photo description",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        testStateFlow.value = PhotoState(photo = testPhoto, isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Photo Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024-01-01").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test photo description").assertIsDisplayed()
    }

    @Test
    fun photoScreen_shareButtonClick_triggersShareAction() {
        // Given - state with photo
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        testStateFlow.value = PhotoState(photo = testPhoto, isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Click share button
        composeTestRule.onNodeWithContentDescription("Share").performClick()

        // Then
        verify { mockViewModel.onSharePhotoClicked() }
    }

    @Test
    fun photoScreen_datePickerButtonClick_triggersDatePicker() {
        // Given - state with photo
        val testPhoto = Photo(
            photoId = "test_id",
            photoTitle = "Test Photo",
            photoDate = "2024-01-01",
            photoUrl = "https://example.com/photo.jpg",
            photoMediaType = "image"
        )
        testStateFlow.value = PhotoState(photo = testPhoto, isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Click calendar button
        composeTestRule.onNodeWithContentDescription("Calendar").performClick()

        // Then
        verify { mockViewModel.onDatePickerClicked() }
    }

    @Test
    fun photoScreen_errorState_displaysErrorMessage() {
        // Given - error state
        testStateFlow.value = PhotoState(
            error = "Failed to load photo",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load photo").assertIsDisplayed()
    }

    @Test
    fun photoScreen_titleDisplay_showsCorrectTitle() {
        // When
        composeTestRule.setContent {
            AstroTheme {
                PhotoScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Astronomy Picture of the Day").assertIsDisplayed()
    }
}