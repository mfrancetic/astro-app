package com.udacity.astroapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

enum class BottomBarDestination(
    val direction: DirectionDestination,
    val icon: ImageVector,
    val label: String
) {
    Home(PhotoScreenDestination, Icons.Default.Camera, "nav_home"),
    Earth(EarthPhotoScreenDestination, Icons.Default.Public, "nav_earth"),
    Asteroids(AsteroidScreenDestination, Icons.Default.Satellite, "nav_asteroids"),
    Mars(MarsPhotoScreenDestination, Icons.Default.Terrain, "nav_mars")
}

// Helper interface for type safety
interface DirectionDestination {
    val route: String
}

// Make destinations implement the interface
object PhotoScreenDestination : DirectionDestination {
    override val route = "photo_screen"
}

object EarthPhotoScreenDestination : DirectionDestination {
    override val route = "earth_photo_screen"
}

object AsteroidScreenDestination : DirectionDestination {
    override val route = "asteroid_screen"
}

object MarsPhotoScreenDestination : DirectionDestination {
    override val route = "mars_photo_screen"
}