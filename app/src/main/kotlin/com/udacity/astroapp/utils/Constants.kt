package com.udacity.astroapp.utils

object Constants {
    const val MARS_PHOTO_BASE_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/"
    const val NASA_API_KEY = Secret.nasa_api_key
    const val PAGE_NUMBER = "1"

    const val EARTH_PHOTO_KEY = "earthPhoto"
    const val EARTH_CURRENT_DATE_KEY = "earthPhotoCurrentDate"
    const val EARTH_SCROLL_POSITION_X_KEY = "earthPhotoX"
    const val EARTH_SCROLL_POSITION_Y_KEY = "earthPhotoY"
    const val EARTH_PHOTOS_KEY = "earthPhotos"

    const val MARS_PHOTO_KEY = "marsPhoto"
    const val MARS_CURRENT_DATE_KEY = "marsPhotoCurrentDate"
    const val MARS_SCROLL_POSITION_X_KEY = "marsPhotoX"
    const val MARS_SCROLL_POSITION_Y_KEY = "marsPhotoY"
    const val MARS_PHOTOS_KEY = "marsPhotos"

    const val DEVELOPER_WEBSITE_URL = "https://mfrancetic.gitlab.io/cv-website/"

    // Database cache durations (content-specific)
    private const val HOUR_IN_MILLIS = 60 * 60 * 1000L

    const val PHOTO_CACHE_DURATION_HOURS = 6L // Daily photos change once per day
    const val ASTEROID_CACHE_DURATION_HOURS = 2L // More dynamic data
    const val EARTH_PHOTO_CACHE_DURATION_HOURS = 4L // Updated several times per day
    const val MARS_PHOTO_CACHE_DURATION_HOURS = 12L // Archive data, changes rarely
    const val OBSERVATORY_CACHE_DURATION_HOURS = 24L // Static location data

    // Computed cache durations in milliseconds
    val PHOTO_CACHE_DURATION_MILLIS
        get() = PHOTO_CACHE_DURATION_HOURS * HOUR_IN_MILLIS

    val ASTEROID_CACHE_DURATION_MILLIS
        get() = ASTEROID_CACHE_DURATION_HOURS * HOUR_IN_MILLIS

    val EARTH_PHOTO_CACHE_DURATION_MILLIS
        get() = EARTH_PHOTO_CACHE_DURATION_HOURS * HOUR_IN_MILLIS

    val MARS_PHOTO_CACHE_DURATION_MILLIS
        get() = MARS_PHOTO_CACHE_DURATION_HOURS * HOUR_IN_MILLIS

    val OBSERVATORY_CACHE_DURATION_MILLIS
        get() = OBSERVATORY_CACHE_DURATION_HOURS * HOUR_IN_MILLIS

    // HTTP cache settings
    const val HTTP_CACHE_SIZE = 50L * 1024 * 1024 // 50MB HTTP cache
    const val HTTP_CACHE_MAX_AGE_HOURS = 1
    const val HTTP_CACHE_MAX_STALE_DAYS = 7

    // Image cache settings
    const val IMAGE_MEMORY_CACHE_SIZE_PERCENT = 0.25 // 25% of available memory
    const val IMAGE_DISK_CACHE_SIZE = 100L * 1024 * 1024 // 100MB disk cache
}
