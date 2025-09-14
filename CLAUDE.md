# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AstroApp is an Android application that displays astronomy-related content from NASA APIs, including daily photos, Earth photos, Mars rover photos, and information about near-Earth asteroids. It was developed as a Capstone project for the Udacity Android Developer Nanodegree program.

## Key Commands

### Build Commands
- `./gradlew build` - Build the entire project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug version on connected device

### Testing Commands
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on connected device
- `./gradlew testDebugUnitTest` - Run debug unit tests

### Code Quality
- `./gradlew lint` - Run Android lint checks
- `./gradlew lintDebug` - Run lint on debug variant

## Architecture Overview

The app follows Android Architecture Components patterns with:

### Package Structure
- `activities/` - Contains MainActivity (single activity with navigation drawer)
- `fragments/` - UI fragments for different content types:
  - `PhotoFragment` - Daily astronomy photo
  - `EarthPhotoFragment` - Earth photos from EPIC API
  - `MarsPhotoFragment` - Mars rover photos
  - `AsteroidFragment` - Near-Earth asteroid information
  - `ObservatoryFragment`/`ObservatoryListFragment` - Observatory locations
- `models/` - Data models (Asteroid, Photo, EarthPhoto, MarsPhoto, Observatory, etc.)
- `data/` - Data layer with Room database, ViewModels, and repository pattern:
  - `AppDatabase` - Room database
  - `AstroDao` - Database access object
  - ViewModels and ViewModelFactories for each content type
  - `AppExecutors` - Background thread management
- `utils/` - Utility classes:
  - `QueryUtils` - NASA API communication (contains API endpoints)
  - `RetrofitClientInstance` - Retrofit setup for Mars photos
  - `PhotoUtils`, `DateTimeUtils`, `WebIntentUtils` - Helper utilities
- `adapters/` - RecyclerView adapters for lists and grids

### Key Technologies
- **UI**: View Binding, Material Design, RecyclerView, Navigation Drawer
- **Database**: Room (SQLite abstraction)
- **Networking**: HttpURLConnection for NASA APIs, Retrofit2 + Gson for Mars photos API
- **Architecture**: MVVM with ViewModels and LiveData
- **Image Loading**: Glide
- **Maps**: Google Maps SDK and Places API for observatory locations
- **Video**: YouTube Player for video content

### API Integration
The app integrates with multiple NASA APIs:
- **APOD (Astronomy Picture of the Day)**: Daily astronomy photos/videos
- **Near Earth Object Web Service**: Asteroid data
- **EPIC**: Earth Polychromatic Imaging Camera photos
- **Mars Rover Photos**: Photos from Mars rovers

**Important**: API keys have been removed from the repository. To use the app, obtain a NASA API key from https://api.nasa.gov/ and add it to `QueryUtils.java` in the `api_key` string variable.

### Build Configuration
- **Target SDK**: 35 (Android 14)
- **Min SDK**: 26 (Android 8.0)
- **Build Tools**: Android Gradle Plugin 8.4.2
- **Java Version**: 8
- **Features**: View Binding enabled, MultiDex enabled
- **Signing**: Release builds are signed using keystore.properties

### Testing Structure
- Unit tests in `src/test/java/`
- Instrumented tests in `src/androidTest/java/` including fragment testing with Espresso
- Test dependencies include JUnit, Espresso, Fragment testing utilities