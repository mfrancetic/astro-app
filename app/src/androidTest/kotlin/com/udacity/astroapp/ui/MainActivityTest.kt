package com.udacity.astroapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_startsCorrectly() {
        // Check that the app starts and shows the bottom navigation
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Earth").assertIsDisplayed()
        composeTestRule.onNodeWithText("Asteroids").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mars").assertIsDisplayed()
    }

    @Test
    fun mainActivity_navigationWorks() {
        // Test navigation between tabs
        composeTestRule.onNodeWithText("Asteroids").performClick()
        
        // Wait a moment for navigation
        composeTestRule.waitForIdle()
        
        // Should now show asteroid screen content
        composeTestRule.onNodeWithText("Near-Earth Asteroids").assertIsDisplayed()
    }

    @Test
    fun mainActivity_homeTabSelected_byDefault() {
        // Home tab should be selected by default (Photo screen)
        composeTestRule.onNodeWithText("Astronomy Picture of the Day").assertIsDisplayed()
    }

    @Test
    fun mainActivity_bottomNavigation_allTabsPresent() {
        // All bottom navigation tabs should be present
        composeTestRule.onNodeWithContentDescription("nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("nav_earth").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("nav_asteroids").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("nav_mars").assertIsDisplayed()
    }

    @Test
    fun mainActivity_earthTabClick_showsEarthScreen() {
        // Click on Earth tab
        composeTestRule.onNodeWithText("Earth").performClick()
        
        composeTestRule.waitForIdle()
        
        // Should show Earth photo screen
        composeTestRule.onNodeWithText("Earth Imagery").assertIsDisplayed()
    }

    @Test
    fun mainActivity_marsTabClick_showsMarsScreen() {
        // Click on Mars tab
        composeTestRule.onNodeWithText("Mars").performClick()
        
        composeTestRule.waitForIdle()
        
        // Should show Mars photo screen
        composeTestRule.onNodeWithText("Mars Rover Photos").assertIsDisplayed()
    }
}