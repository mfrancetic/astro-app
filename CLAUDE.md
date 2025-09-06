# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AstroApp is an Android application that displays astronomy-related content from NASA APIs. It presents daily photos, Earth photos, Mars rover photos, and asteroid data, with a local Room database for offline storage and Google Maps integration for observatories.

**Migration Status**: This project is currently in a **hybrid state** during migration from Java/Fragments to Kotlin/Compose. Both architectures coexist, with the modern Kotlin/Compose implementation being the target architecture.

## Key Architecture Components

### Modern Architecture (Kotlin/Compose - Target Implementation)

#### UI Framework
- **Jetpack Compose** with Material 3 design system
- **Compose Destinations** for type-safe navigation
- **Landscapist** for async image loading in Compose
- **Compose BOM**: 2024.02.00

#### Architecture Pattern
- **MVVM with MVI** using Orbit MVI library
- **Repository Pattern** with proper separation of concerns
- **Koin Dependency Injection** for DI container
- **Kotlin Coroutines** for asynchronous operations
- **StateFlow/Flow** for reactive data streams

#### Database Layer (Room - Migrated to Kotlin)
- **AppDatabase.kt** - Room database with entities: Photo, Asteroid, Observatory, EarthPhoto, MarsPhoto
- **AstroDao.kt** - Data access object with suspend functions and Flow
- **Converters.kt** - Type converters for complex data types
- **DatabaseModule.kt** - Koin DI module for database dependencies

#### Core Components
- **MainActivity.kt** - Single activity with Compose navigation
- **Screens** in `ui/screens/` package: HomeScreen, PhotoScreen, AsteroidScreen, etc.
- **ViewModels** with Orbit MVI pattern in `ui/viewmodel/` package
- **Repositories** in `repository/` package
- **Models** as Kotlin data classes with Parcelize annotation

### Legacy Architecture (Java/Fragments - Being Phased Out)
- **MainActivity.java** - Original fragment-based navigation (deprecated)
- **Fragments** for each feature (being replaced by Compose screens)
- **ViewModels** with ViewModelFactory classes (legacy)
- **LiveData** for reactive UI updates (replaced by StateFlow)

### API Integration
- **NetworkUtils.kt** - Centralized API URL building and HTTP requests (partially migrated)
- **RetrofitClientInstance.java** - Retrofit configuration (legacy)
- **Secret.kt** - API key storage (NASA and Google Maps)
- **Models** with proper Kotlin serialization support

### Dependency Injection (Koin)
- **AppModule.kt** - Main application DI module
- **DatabaseModule.kt** - Database-related dependencies
- **NetworkModule.kt** - Network and API dependencies
- **Application.kt** - Koin initialization

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
./gradlew detekt                 # Run Detekt static analysis (if configured)
```

### Clean Build
```bash
./gradlew clean                  # Clean build artifacts
./gradlew clean assembleDebug   # Clean build and rebuild debug
```

## Configuration Requirements

### API Keys Setup
1. Create `keystore.properties` in root directory with signing configuration
2. Add NASA API key in `Secret.kt` (NASA_API_KEY)
3. Add Google Maps API key in `Secret.kt` (GOOGLE_PLAY_SERVICES_API_KEY)
4. API keys can be obtained from:
   - NASA: https://api.nasa.gov/index.html#apply-for-an-api-key
   - Google Maps: Google Cloud Console

### Build Configuration
- **Minimum SDK**: 24 (Android 7.0)
- **Target/Compile SDK**: 35 (Android 14)
- **Kotlin**: 1.9.22
- **Java Version**: 1.8
- **Compose BOM**: 2024.02.00
- **View Binding**: Enabled (legacy support)
- **MultiDex**: Enabled for legacy support

### Key Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Orbit MVI** - MVI architecture library
- **Koin** - Dependency injection framework
- **Compose Destinations** - Type-safe navigation
- **Room** - Local database with Kotlin coroutines
- **Landscapist** - Async image loading for Compose

## Important Development Notes

### Migration Guidelines
- **Prefer Kotlin/Compose** implementation for new features
- **Legacy Java/Fragment code** should be migrated incrementally
- **Database layer** has been fully migrated to Kotlin
- **UI screens** should use Compose with Material 3 design
- **ViewModels** should follow Orbit MVI pattern

### Architecture Patterns
- Use **Koin DI** for dependency injection
- Implement **Repository pattern** for data access
- Follow **MVI pattern** with Orbit for state management
- Use **Kotlin coroutines** for asynchronous operations
- Leverage **StateFlow/Flow** for reactive programming

### API Integration
- API calls should use **suspend functions** with coroutines
- Error handling with **Result** sealed class pattern
- Network connectivity checks before API calls
- Image loading with **Landscapist** in Compose screens

### Database Operations
- All database operations use **suspend functions**
- Query results return **Flow** for reactive updates
- Entity relationships properly mapped with Room annotations
- Database version management with Room migrations

### Testing Structure
- **Unit tests** in `src/test/` for ViewModels and repositories
- **Compose UI tests** in `src/androidTest/` using ComposeTestRule
- **Integration tests** for database operations
- **Mock dependencies** using Koin test modules

### UI Guidelines
- Use **Material 3 components** in Compose
- Follow **Material Design** principles
- Implement **proper theming** with color schemes
- Support **dark/light mode** theming
- Ensure **accessibility** compliance

### Permissions
- INTERNET and ACCESS_NETWORK_STATE for API calls
- READ_EXTERNAL_STORAGE (maxSdkVersion 32) for image handling
- Location permissions requested at runtime for observatory features
- Proper runtime permission handling in Compose screens

### Localization
- Supports Croatian (hr) and English (en) via resConfigs
- String resources properly externalized
- Multi-language support in Compose screens
- Language switching handled by system settings

## Migration Progress Tracking

### ✅ Completed
- Database layer migrated to Kotlin with Room + Coroutines
- Models converted to Kotlin data classes with Parcelize
- Dependency injection setup with Koin
- Core Compose screens implemented (Photo, Asteroid, Earth, Mars, Observatory)
- Navigation with Compose Destinations
- Modern MVVM/MVI architecture established
- API integration layer migration (QueryUtils.kt with full implementation)
- Complete Fragment to Compose screen migration
- Observatory Compose screens with ViewModels and DI setup
- Legacy Java Fragment implementations removed
- Widget implementation updated for Kotlin architecture
- Final cleanup of deprecated code completed
- Testing structure fully updated for Compose with modern test framework

### 🔄 In Progress
- None - all migration tasks complete!

### ❌ Pending
- None - migration is complete!