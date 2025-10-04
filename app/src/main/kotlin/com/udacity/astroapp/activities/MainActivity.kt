package com.udacity.astroapp.activities

import android.content.Context
import android.net.ConnectivityManager
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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.udacity.astroapp.R
import com.udacity.astroapp.ui.screens.NavGraphs
import com.udacity.astroapp.ui.screens.destinations.AsteroidScreenDestination
import com.udacity.astroapp.ui.screens.destinations.EarthPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.MarsPhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.PhotoScreenDestination
import com.udacity.astroapp.ui.screens.destinations.SettingsScreenDestination
import com.udacity.astroapp.ui.theme.AstroAppTheme
import com.udacity.astroapp.utils.ThemePreferenceManager
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.get

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
                val themePreferenceManager = get<ThemePreferenceManager>()
                val themePreference by themePreferenceManager.themePreference.collectAsState()

                AstroAppTheme(themePreference = themePreference) {
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

    private fun getScreenTitle(tabIndex: Int): Int {
        return when (tabIndex) {
            0 -> R.string.screen_title_photo
            1 -> R.string.screen_title_asteroids
            2 -> R.string.screen_title_earth_photo
            3 -> R.string.screen_title_mars_photo
            4 -> R.string.settings_about_title
            else -> R.string.app_name
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    fun AstroAppContent() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        // Tab items
        val tabItems =
            listOf(
                TabItem(stringResource(R.string.tab_daily_photo), Icons.Default.Home),
                TabItem(stringResource(R.string.tab_asteroid), Icons.Default.Satellite),
                TabItem(stringResource(R.string.tab_earth), Icons.Default.Public),
                TabItem(stringResource(R.string.tab_mars), Icons.Default.AccountTree),
                TabItem(stringResource(R.string.tab_settings), Icons.Default.Settings)
            )

        Scaffold(
            bottomBar = {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabItems.forEachIndexed { index, tabItem ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(imageVector = tabItem.icon, contentDescription = tabItem.label)
                            },
                            text = { Text(tabItem.label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavigationHost(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    @Composable
    fun NavigationHost(selectedTabIndex: Int, modifier: Modifier = Modifier) {
        val navEngine = rememberNavHostEngine()
        val navController = navEngine.rememberNavController()

        LaunchedEffect(selectedTabIndex) {
            val currentDestination =
                when (selectedTabIndex) {
                    0 -> PhotoScreenDestination
                    1 -> AsteroidScreenDestination
                    2 -> EarthPhotoScreenDestination
                    3 -> MarsPhotoScreenDestination
                    4 -> SettingsScreenDestination
                    else -> PhotoScreenDestination
                }

            navController.navigate(currentDestination.route) {
                popUpTo(PhotoScreenDestination.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            engine = navEngine,
            modifier = modifier
        )
    }

    data class TabItem(
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )
}
