package com.udacity.astroapp.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.udacity.astroapp.R

@Composable
fun AstroBottomNavigation(
    navController: NavController,
    destinations: List<BottomBarDestination>
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar {
        destinations.forEach { destination ->
            val isCurrentDestOnBackStack = navController.isRouteOnBackStack(destination.direction)
            
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(destination.direction, false)
                        return@NavigationBarItem
                    }

                    navController.navigate(destination.direction) {
                        // Pop up to the root of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = { 
                    Icon(
                        destination.icon, 
                        contentDescription = getDestinationLabel(destination.label)
                    ) 
                },
                label = { 
                    Text(getDestinationLabel(destination.label)) 
                }
            )
        }
    }
}

@Composable
private fun getDestinationLabel(labelKey: String): String {
    return when (labelKey) {
        "nav_home" -> stringResource(R.string.nav_home)
        "nav_earth" -> stringResource(R.string.nav_earth)
        "nav_asteroids" -> stringResource(R.string.nav_asteroids)
        "nav_mars" -> stringResource(R.string.nav_mars)
        else -> labelKey
    }
}