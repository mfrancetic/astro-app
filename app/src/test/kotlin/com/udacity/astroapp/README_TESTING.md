# AstroApp Testing Guide

This document describes the modern testing structure for AstroApp after the Kotlin/Compose migration.

## Test Architecture

### Unit Tests (`src/test/kotlin`)
- **ViewModels**: Test MVI state management using Orbit Test utilities
- **Repositories**: Test data layer logic with MockK and coroutine testing
- **Utils**: Test utility functions and helpers

### Instrumented Tests (`src/androidTest/kotlin`) 
- **Compose UI Tests**: Test Compose screens using ComposeTestRule
- **Database Tests**: Test Room database operations
- **Integration Tests**: Test MainActivity and navigation

## Testing Libraries Used

### Unit Testing
- **JUnit 4**: Core testing framework
- **MockK**: Kotlin-first mocking library
- **Orbit Test**: MVI testing utilities for Orbit ViewModels
- **Kotlinx Coroutines Test**: Coroutine testing utilities
- **Koin Test**: Dependency injection testing support

### Instrumented Testing
- **Compose UI Test**: Jetpack Compose testing framework
- **Room Testing**: Database testing utilities
- **AndroidX Test**: Android testing framework
- **MockK Android**: Android-specific MockK features

## Test Structure

```
src/test/kotlin/
├── com/udacity/astroapp/
│   ├── ui/screens/
│   │   ├── photo/PhotoViewModelTest.kt
│   │   ├── asteroid/AsteroidViewModelTest.kt
│   │   └── ...
│   ├── repository/
│   │   ├── PhotoRepositoryTest.kt
│   │   ├── AsteroidRepositoryTest.kt
│   │   └── ...
│   ├── data/AstroDaoTest.kt
│   ├── utils/QueryUtilsTest.kt
│   ├── TestApplication.kt
│   └── ExampleUnitTest.kt

src/androidTest/kotlin/
├── com/udacity/astroapp/
│   ├── ui/
│   │   ├── MainActivityTest.kt
│   │   └── screens/
│   │       ├── photo/PhotoScreenTest.kt
│   │       ├── asteroid/AsteroidScreenTest.kt
│   │       ├── observatory/ObservatoryListScreenTest.kt
│   │       └── ...
│   ├── data/AstroDaoTest.kt
│   └── ExampleInstrumentedTest.kt
```

## Running Tests

### Command Line
```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests PhotoViewModelTest

# Run tests with coverage
./gradlew testDebugUnitTestCoverage
```

### Android Studio
- Right-click on test files or packages
- Select "Run Tests" or "Run with Coverage"
- Use the Test Results window to view results

## Writing New Tests

### ViewModel Tests (MVI with Orbit)
```kotlin
@Test
fun `viewModel should load data successfully`() = runTest {
    // Given
    val testData = createTestData()
    coEvery { repository.getData() } returns testData
    
    // When & Then
    viewModel.test(this) {
        expectInitialState(InitialState())
        containerHost.loadData()
        
        expectState { LoadingState() }
        expectState { SuccessState(testData) }
    }
}
```

### Compose UI Tests
```kotlin
@Test
fun `screen should display data correctly`() {
    // Given
    val testState = ScreenState(data = testData)
    every { mockViewModel.container.stateFlow } returns MutableStateFlow(testState)
    
    // When
    composeTestRule.setContent {
        AstroTheme {
            TestScreen(viewModel = mockViewModel)
        }
    }
    
    // Then
    composeTestRule.onNodeWithText("Expected Text").assertIsDisplayed()
}
```

### Repository Tests
```kotlin
@Test
fun `repository should cache data correctly`() = runTest {
    // Given
    val testData = createTestData()
    coEvery { dao.getData() } returns cachedData
    coEvery { api.fetchData() } returns testData
    
    // When
    val result = repository.getData(forceRefresh = false)
    
    // Then
    assertEquals(cachedData, result)
    coVerify { dao.getData() }
    coVerify(exactly = 0) { api.fetchData() }
}
```

## Best Practices

1. **Use descriptive test names** with backticks for readability
2. **Follow Given-When-Then structure** in test methods
3. **Mock external dependencies** using MockK
4. **Test both success and error scenarios**
5. **Use test data builders** for complex objects
6. **Verify interactions** with mocked dependencies
7. **Test UI states** thoroughly in Compose tests
8. **Use proper test scopes** for coroutines

## Continuous Integration

Tests run automatically on:
- Pull requests
- Main branch commits
- Scheduled nightly builds

Ensure all tests pass before merging code.