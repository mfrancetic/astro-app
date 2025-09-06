package com.udacity.astroapp.ui.screens.observatory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.ui.theme.AstroTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObservatoryListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ObservatoryListViewModel
    private lateinit var mockNavigator: DestinationsNavigator
    private lateinit var testStateFlow: MutableStateFlow<ObservatoryListState>

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        mockNavigator = mockk(relaxed = true)
        testStateFlow = MutableStateFlow(ObservatoryListState())
        
        every { mockViewModel.container.stateFlow } returns testStateFlow
        every { mockViewModel.container.sideEffectFlow } returns MutableStateFlow()
    }

    @Test
    fun observatoryListScreen_initialLoading_showsLoadingIndicator() {
        // Given - loading state
        testStateFlow.value = ObservatoryListState(isLoading = true)

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading observatories").assertIsDisplayed()
    }

    @Test
    fun observatoryListScreen_withObservatories_displaysObservatoryList() {
        // Given - state with observatories
        val testObservatories = listOf(
            Observatory(
                observatoryId = "1",
                observatoryName = "Test Observatory 1",
                observatoryAddress = "123 Test Street, Test City",
                observatoryLatitude = 40.7128,
                observatoryLongitude = -74.0060,
                observatoryOpenNow = true
            ),
            Observatory(
                observatoryId = "2",
                observatoryName = "Test Observatory 2",
                observatoryAddress = "456 Test Avenue, Test Town",
                observatoryLatitude = 34.0522,
                observatoryLongitude = -118.2437,
                observatoryOpenNow = false
            )
        )
        testStateFlow.value = ObservatoryListState(
            observatories = testObservatories,
            isLoading = false,
            hasLocationPermission = true
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Observatory 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Observatory 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("123 Test Street, Test City").assertIsDisplayed()
        composeTestRule.onNodeWithText("456 Test Avenue, Test Town").assertIsDisplayed()
    }

    @Test
    fun observatoryListScreen_observatoryOpen_showsOpenStatus() {
        // Given - state with open observatory
        val openObservatory = Observatory(
            observatoryId = "1",
            observatoryName = "Open Observatory",
            observatoryAddress = "123 Test Street",
            observatoryLatitude = 40.7128,
            observatoryLongitude = -74.0060,
            observatoryOpenNow = true
        )
        testStateFlow.value = ObservatoryListState(
            observatories = listOf(openObservatory),
            isLoading = false,
            hasLocationPermission = true
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Open now").assertIsDisplayed()
    }

    @Test
    fun observatoryListScreen_noLocationPermission_showsPermissionBanner() {
        // Given - state without location permission
        testStateFlow.value = ObservatoryListState(
            observatories = emptyList(),
            isLoading = false,
            hasLocationPermission = false
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Location permission declined").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grant").assertIsDisplayed()
    }

    @Test
    fun observatoryListScreen_observatoryClick_triggersNavigation() {
        // Given - state with observatory
        val testObservatory = Observatory(
            observatoryId = "test_id",
            observatoryName = "Test Observatory",
            observatoryAddress = "123 Test Street",
            observatoryLatitude = 40.7128,
            observatoryLongitude = -74.0060
        )
        testStateFlow.value = ObservatoryListState(
            observatories = listOf(testObservatory),
            isLoading = false,
            hasLocationPermission = true
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Click on observatory item
        composeTestRule.onNodeWithText("Test Observatory").performClick()

        // Then
        verify { mockViewModel.onObservatoryClicked(testObservatory) }
    }

    @Test
    fun observatoryListScreen_emptyState_displaysEmptyMessage() {
        // Given - empty state with location permission
        testStateFlow.value = ObservatoryListState(
            observatories = emptyList(),
            isLoading = false,
            hasLocationPermission = true,
            isLocationEnabled = true
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No observatories found").assertIsDisplayed()
    }

    @Test
    fun observatoryListScreen_titleDisplay_showsCorrectTitle() {
        // When
        composeTestRule.setContent {
            AstroTheme {
                ObservatoryListScreen(
                    navigator = mockNavigator,
                    viewModel = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Observatories").assertIsDisplayed()
    }
}