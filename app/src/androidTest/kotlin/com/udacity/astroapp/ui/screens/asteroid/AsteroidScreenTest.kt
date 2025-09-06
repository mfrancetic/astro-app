package com.udacity.astroapp.ui.screens.asteroid

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.astroapp.models.Asteroid
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
class AsteroidScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: AsteroidViewModel
    private lateinit var testStateFlow: MutableStateFlow<AsteroidState>

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        testStateFlow = MutableStateFlow(AsteroidState())
        
        every { mockViewModel.container.stateFlow } returns testStateFlow
        every { mockViewModel.container.sideEffectFlow } returns MutableStateFlow()
    }

    @Test
    fun asteroidScreen_initialLoading_showsLoadingIndicator() {
        // Given - loading state
        testStateFlow.value = AsteroidState(isLoading = true)

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun asteroidScreen_withAsteroids_displaysAsteroidList() {
        // Given - state with asteroids
        val testAsteroids = listOf(
            Asteroid(
                asteroidId = "1",
                asteroidName = "Test Asteroid 1",
                asteroidApproachDate = "2024-01-01",
                asteroidIsPotentiallyDangerous = true,
                asteroidVelocity = 50000.0,
                asteroidDistanceFromEarth = 1000000.0,
                asteroidMinDiameter = 1.0,
                asteroidMaxDiameter = 2.0,
                asteroidAbsoluteMagnitude = 18.5
            ),
            Asteroid(
                asteroidId = "2",
                asteroidName = "Test Asteroid 2",
                asteroidApproachDate = "2024-01-02",
                asteroidIsPotentiallyDangerous = false,
                asteroidVelocity = 30000.0,
                asteroidDistanceFromEarth = 2000000.0,
                asteroidMinDiameter = 0.5,
                asteroidMaxDiameter = 1.0,
                asteroidAbsoluteMagnitude = 19.2
            )
        )
        testStateFlow.value = AsteroidState(asteroids = testAsteroids, isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Asteroid 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Asteroid 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024-01-01").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024-01-02").assertIsDisplayed()
    }

    @Test
    fun asteroidScreen_dangerousAsteroid_showsDangerousIndicator() {
        // Given - state with dangerous asteroid
        val dangerousAsteroid = Asteroid(
            asteroidId = "1",
            asteroidName = "Dangerous Asteroid",
            asteroidApproachDate = "2024-01-01",
            asteroidIsPotentiallyDangerous = true,
            asteroidVelocity = 50000.0,
            asteroidDistanceFromEarth = 1000000.0,
            asteroidMinDiameter = 1.0,
            asteroidMaxDiameter = 2.0,
            asteroidAbsoluteMagnitude = 18.5
        )
        testStateFlow.value = AsteroidState(asteroids = listOf(dangerousAsteroid), isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Potentially Dangerous").assertIsDisplayed()
    }

    @Test
    fun asteroidScreen_asteroidClick_triggersDetailView() {
        // Given - state with asteroid
        val testAsteroid = Asteroid(
            asteroidId = "1",
            asteroidName = "Test Asteroid",
            asteroidApproachDate = "2024-01-01",
            asteroidIsPotentiallyDangerous = false,
            asteroidVelocity = 30000.0,
            asteroidDistanceFromEarth = 1000000.0,
            asteroidMinDiameter = 1.0,
            asteroidMaxDiameter = 2.0,
            asteroidAbsoluteMagnitude = 18.5
        )
        testStateFlow.value = AsteroidState(asteroids = listOf(testAsteroid), isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Click on asteroid item
        composeTestRule.onNodeWithText("Test Asteroid").performClick()

        // Then
        verify { mockViewModel.onAsteroidClicked(testAsteroid) }
    }

    @Test
    fun asteroidScreen_emptyState_displaysEmptyMessage() {
        // Given - empty state
        testStateFlow.value = AsteroidState(asteroids = emptyList(), isLoading = false)

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("No asteroids found").assertIsDisplayed()
    }

    @Test
    fun asteroidScreen_errorState_displaysErrorMessage() {
        // Given - error state
        testStateFlow.value = AsteroidState(
            error = "Failed to load asteroids",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load asteroids").assertIsDisplayed()
    }

    @Test
    fun asteroidScreen_titleDisplay_showsCorrectTitle() {
        // When
        composeTestRule.setContent {
            AstroTheme {
                AsteroidScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Near-Earth Asteroids").assertIsDisplayed()
    }
}