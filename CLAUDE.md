# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AstroApp is an Android application that displays astronomy-related content from NASA APIs. It presents daily photos, Earth photos, Mars rover photos, and asteroid data, with a local Room database for offline storage and Google Maps integration for observatories.

## Key Architecture Components

### Database Layer (Room)
- **AppDatabase.java** - Room database with entities: Photo, Asteroid, Observatory, EarthPhoto, MarsPhoto
- **AstroDao.java** - Data access object with CRUD operations
- **Converters.java** - Type converters for complex data types

### MVVM Pattern
- ViewModels with ViewModelFactory classes for each major feature
- Repository pattern implemented through database and API interactions
- LiveData for reactive UI updates

### Fragment-Based Navigation
- **MainActivity.java** - Single activity with drawer navigation
- Fragments for each feature: PhotoFragment, AsteroidFragment, EarthPhotoFragment, MarsPhotoFragment, ObservatoryFragment
- Fragment communication through interfaces (OnObservatoryClickListener)

### API Integration
- **QueryUtils.java** - Centralized API URL building and HTTP requests
- **RetrofitClientInstance.java** - Retrofit configuration for Mars rover API
- **Secret.java** - API key storage (NASA and Google Maps)

### Key Features
- NASA APOD (Astronomy Picture of the Day)
- Near-Earth asteroids tracking
- Mars rover photo galleries
- Earth satellite imagery (EPIC)
- Observatory finder with Google Maps/Places integration
- Home screen widget (AstroAppWidget)

## Development Commands

### Build and Run
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install debug APK on connected device
./gradlew assembleRelease        # Build release APK (requires keystore.properties)
```

### Testing
```bash
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires connected device/emulator)
./gradlew testDebugUnitTest      # Run debug unit tests specifically
```

### Code Quality
```bash
./gradlew lint                   # Run Android lint checks
./gradlew lintDebug             # Run lint on debug build
```

### Clean Build
```bash
./gradlew clean                  # Clean build artifacts
./gradlew clean assembleDebug   # Clean build and rebuild debug
```

## Configuration Requirements

### API Keys Setup
1. Create `keystore.properties` in root directory with signing configuration
2. Add NASA API key in `Secret.java` (nasa_api_key)
3. Add Google Maps API key in `Secret.java` (google_play_services_api_key)
4. API keys can be obtained from:
   - NASA: https://api.nasa.gov/index.html#apply-for-an-api-key
   - Google Maps: Google Cloud Console

### Build Configuration
- **Minimum SDK**: 19 (Android 4.4)
- **Target SDK**: 35 (Android 14)
- **Java Version**: 1.8
- **View Binding**: Enabled
- **MultiDex**: Enabled for legacy support

## Important Development Notes

### API Integration
- All NASA API calls go through QueryUtils.java
- Mars rover photos use Retrofit with custom service interface
- Error handling includes network connectivity checks
- Image loading handled by Glide with custom module (AstroGlideAppModule)

### Database Migrations
- Current database version: 3
- Room handles schema changes automatically with version increments
- Database entities are in models package

### Testing Structure
- Unit tests in `src/test/`
- Instrumented tests in `src/androidTest/` 
- Fragment testing uses FragmentTestRule and Espresso
- Test helper class: AndroidTestHelper.java

### Permissions
- INTERNET and ACCESS_NETWORK_STATE for API calls
- READ_EXTERNAL_STORAGE (maxSdkVersion 32) for image handling
- Location permissions requested at runtime for observatory features

### Localization
- Supports Croatian (hr) and English (en) via resConfigs
- Language switching handled by LanguageHelper.java