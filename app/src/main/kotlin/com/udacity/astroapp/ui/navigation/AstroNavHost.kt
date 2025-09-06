package com.udacity.astroapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.udacity.astroapp.ui.screens.asteroid.AsteroidScreen
import com.udacity.astroapp.ui.screens.earth.EarthPhotoScreen
import com.udacity.astroapp.ui.screens.mars.MarsPhotoScreen
import com.udacity.astroapp.ui.screens.photo.PhotoScreen

@Composable
fun AstroNavHost(
    navController: NavHostController,
    startDestination: String = AstroScreen.HOME.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AstroScreen.HOME.route) {
            PhotoScreen()
        }
        
        composable(AstroScreen.EARTH.route) {
            EarthPhotoScreen()
        }
        
        composable(AstroScreen.ASTEROIDS.route) {
            AsteroidScreen()
        }
        
        composable(AstroScreen.MARS.route) {
            MarsPhotoScreen()
        }
    }
}