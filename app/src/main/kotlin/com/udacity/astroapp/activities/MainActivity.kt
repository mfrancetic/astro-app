package com.udacity.astroapp.activities

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.udacity.astroapp.R
import com.udacity.astroapp.navigation.NavigationCallbacks
import com.udacity.astroapp.ui.screens.NavGraphs
import com.udacity.astroapp.ui.screens.destinations.ObservatoryDetailScreenDestination
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.Constants
import com.udacity.astroapp.utils.WebIntentUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {

    companion object {
        // These will be replaced with context.getString() calls in usage
        var isBeingRefreshed = false

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Handle permission result if needed
        }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize theme
        val sharedPreferences = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE)
        val themeId =
            sharedPreferences.getInt(
                getString(R.string.pref_theme_key),
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        AppCompatDelegate.setDefaultNightMode(themeId)

        setContent {
            KoinAndroidContext {
                AstroAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AstroAppContent()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    fun AstroAppContent() {
        val navEngine = rememberNavHostEngine()
        val navController = navEngine.rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var showThemeDialog by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val sharedPreferences = remember {
            context.getSharedPreferences(
                context.getString(R.string.pref_name),
                Context.MODE_PRIVATE
            )
        }

        // Navigation callbacks implementation
        val navigationCallbacks =
            object : NavigationCallbacks {
                override fun onNavigateToAsteroidDetails(asteroidId: String) {
                    // Handle asteroid details navigation
                    // For now, we don't have a details screen, so we'll skip this
                }

                override fun onNavigateToObservatoryDetails(observatoryId: Int) {
                    navController.navigate(
                        ObservatoryDetailScreenDestination(observatoryId = observatoryId)
                    )
                }

                override fun onNavigateBack() {
                    navController.popBackStack()
                }

                override fun onNavigateToPhoto() {
                    navController.popBackStack()
                }

                override fun onNavigateToAsteroids() {
                    navController.popBackStack()
                }

                override fun onNavigateToEarthPhotos() {
                    navController.popBackStack()
                }

                override fun onNavigateToMarsPhotos() {
                    navController.popBackStack()
                }

                override fun onNavigateToObservatories() {
                    navController.popBackStack()
                }
            }

        // Navigation items
        val navigationItems =
            listOf(
                NavigationItem(
                    stringResource(R.string.route_photo),
                    stringResource(R.string.nav_photo),
                    Icons.Default.Home
                ),
                NavigationItem(
                    stringResource(R.string.route_asteroids),
                    stringResource(R.string.nav_asteroids),
                    Icons.Default.Satellite
                ),
                NavigationItem(
                    stringResource(R.string.route_earth_photos),
                    stringResource(R.string.nav_earth_photo),
                    Icons.Default.Public
                ),
                NavigationItem(
                    stringResource(R.string.route_mars_photos),
                    stringResource(R.string.nav_mars_photo),
                    Icons.Default.AccountTree
                ),
                NavigationItem(
                    stringResource(R.string.route_observatories),
                    stringResource(R.string.nav_observatories),
                    Icons.Default.LocationOn
                )
            )

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    // Navigation header
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))
                    )

                    // Navigation items
                    navigationItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected =
                                false, // We'll handle selection differently with destinations
                            onClick = {
                                // Handle navigation with destinations
                                // For now, we'll close the drawer
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    // Theme setting
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.menu_theme)) },
                        selected = false,
                        onClick = {
                            showThemeDialog = true
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    // About
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_about)) },
                        selected = false,
                        onClick = {
                            WebIntentUtils.openWebsiteFromStringUrl(
                                context,
                                Constants.DEVELOPER_WEBSITE_URL
                            )
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        ) {
            Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) {
                paddingValues ->
                DestinationsNavHost(
                    engine = navEngine,
                    navGraph = NavGraphs.root,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Theme dialog
        if (showThemeDialog) {
            ThemeSelectionDialog(
                sharedPreferences = sharedPreferences,
                onDismiss = { showThemeDialog = false }
            )
        }
    }

    @Composable
    private fun ThemeSelectionDialog(sharedPreferences: SharedPreferences, onDismiss: () -> Unit) {
        val context = LocalContext.current
        val androidVersion = Build.VERSION.SDK_INT

        val themes =
            if (androidVersion >= Build.VERSION_CODES.Q) {
                context.resources.getStringArray(R.array.themes_array_v29)
            } else {
                context.resources.getStringArray(R.array.themes_array_default)
            }

        var selectedTheme by remember {
            mutableIntStateOf(
                sharedPreferences.getInt(context.getString(R.string.pref_checked_theme_key), 0)
            )
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.menu_theme)) },
            text = {
                // Theme selection logic would go here
                // For now, using a simple text
                Text(stringResource(R.string.theme_selection_placeholder))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val themeId =
                            when (selectedTheme) {
                                0 -> AppCompatDelegate.MODE_NIGHT_NO
                                1 -> AppCompatDelegate.MODE_NIGHT_YES
                                else ->
                                    if (androidVersion >= Build.VERSION_CODES.Q) {
                                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                    } else {
                                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                                    }
                            }

                        sharedPreferences.edit().apply {
                            putInt(context.getString(R.string.pref_theme_key), themeId)
                            putInt(
                                context.getString(R.string.pref_checked_theme_key),
                                selectedTheme
                            )
                            apply()
                        }

                        AppCompatDelegate.setDefaultNightMode(themeId)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }

    data class NavigationItem(
        val route: String,
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )
}
