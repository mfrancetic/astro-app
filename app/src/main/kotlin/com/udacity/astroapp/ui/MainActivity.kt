package com.udacity.astroapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.udacity.astroapp.ui.navigation.AstroBottomNavigation
import com.udacity.astroapp.ui.navigation.BottomBarDestination
import com.udacity.astroapp.ui.screens.NavGraphs
import com.udacity.astroapp.ui.screens.asteroid.AsteroidScreen
import com.udacity.astroapp.ui.screens.destinations.AsteroidScreenDestination
import com.udacity.astroapp.ui.screens.destinations.EarthPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.MarsPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.PhotoScreenDestination
import com.udacity.astroapp.ui.screens.earth.EarthPhotoScreen
import com.udacity.astroapp.ui.screens.mars.MarsPhotoScreen
import com.udacity.astroapp.ui.screens.photo.PhotoScreen
import com.udacity.astroapp.ui.theme.AstroTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            AstroTheme {
                AstroApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstroApp() {
    val navController = rememberNavController()
    val navHostEngine = rememberNavHostEngine()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AstroBottomNavigation(
                navController = navController,
                destinations = BottomBarDestination.values().toList()
            )
        }
    ) { innerPadding ->
        DestinationsNavHost(
            engine = navHostEngine,
            navController = navController,
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(PhotoScreenDestination) {
                PhotoScreen()
            }
            
            composable(EarthPhotoScreenDestination) {
                EarthPhotoScreen()
            }
            
            composable(AsteroidScreenDestination) {
                AsteroidScreen()
            }
            
            composable(MarsPhotoScreenDestination) {
                MarsPhotoScreen()
            }
        }
    }
}