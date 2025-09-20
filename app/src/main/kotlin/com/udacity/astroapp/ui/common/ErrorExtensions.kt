package com.udacity.astroapp.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.udacity.astroapp.R

@Composable
fun ErrorType.toLocalizedMessage(): String =
    when (this) {
        ErrorType.NETWORK_ERROR -> stringResource(R.string.error_network)
        ErrorType.LOADING_PHOTOS_ERROR -> stringResource(R.string.failed_to_load_photos)
        ErrorType.LOADING_EARTH_PHOTOS_ERROR -> stringResource(R.string.failed_to_load_earth_photos)
        ErrorType.LOADING_MARS_PHOTOS_ERROR -> stringResource(R.string.failed_to_load_mars_photos)
        ErrorType.LOADING_ASTEROIDS_ERROR -> stringResource(R.string.failed_to_load_asteroids)
        ErrorType.LOADING_OBSERVATORIES_ERROR ->
            stringResource(R.string.failed_to_load_observatories)
        ErrorType.LOADING_OBSERVATORY_ERROR -> stringResource(R.string.failed_to_load_observatory)
        ErrorType.GENERIC_ERROR -> stringResource(R.string.error_generic)
    }
