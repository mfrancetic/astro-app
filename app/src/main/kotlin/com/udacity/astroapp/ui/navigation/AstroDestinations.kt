package com.udacity.astroapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.udacity.astroapp.ui.screens.destinations.AsteroidScreenDestination
import com.udacity.astroapp.ui.screens.destinations.EarthPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.MarsPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.PhotoScreenDestination

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    val label: String
) {
    Home(PhotoScreenDestination, Icons.Default.Camera, "nav_home"),
    Earth(EarthPhotoScreenDestination, Icons.Default.Public, "nav_earth"),
    Asteroids(AsteroidScreenDestination, Icons.Default.Satellite, "nav_asteroids"),
    Mars(MarsPhotoScreenDestination, Icons.Default.Terrain, "nav_mars")
}