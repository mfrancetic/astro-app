package com.udacity.astroapp.navigation

/**
 * Interface defining navigation callbacks for inter-screen navigation. These callbacks handle
 * navigation between different screens in the app. UI-specific actions like share and fullscreen
 * remain at the screen level.
 */
interface NavigationCallbacks {

    /**
     * Navigate to asteroid details screen
     *
     * @param asteroidId ID of the asteroid to display
     */
    fun onNavigateToAsteroidDetails(asteroidId: String)

    /**
     * Navigate to observatory details screen
     *
     * @param observatoryId ID of the observatory to display
     */
    fun onNavigateToObservatoryDetails(observatoryId: Int)

    /** Navigate back to the previous screen */
    fun onNavigateBack()

    /** Navigate to photo screen */
    fun onNavigateToPhoto()

    /** Navigate to asteroids screen */
    fun onNavigateToAsteroids()

    /** Navigate to Earth photos screen */
    fun onNavigateToEarthPhotos()

    /** Navigate to Mars photos screen */
    fun onNavigateToMarsPhotos()

    /** Navigate to observatories list screen */
    fun onNavigateToObservatories()
}

/** Empty implementation of NavigationCallbacks for testing purposes */
class EmptyNavigationCallbacks : NavigationCallbacks {
    override fun onNavigateToAsteroidDetails(asteroidId: String) = Unit

    override fun onNavigateToObservatoryDetails(observatoryId: Int) = Unit

    override fun onNavigateBack() = Unit

    override fun onNavigateToPhoto() = Unit

    override fun onNavigateToAsteroids() = Unit

    override fun onNavigateToEarthPhotos() = Unit

    override fun onNavigateToMarsPhotos() = Unit

    override fun onNavigateToObservatories() = Unit
}
