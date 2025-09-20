# Repository Guidelines

## Project Structure & Module Organization
AstroApp is a single Android application module under `app/`. Jetpack Compose code lives in `app/src/main/kotlin/com/udacity/astroapp/ui`, split into `components`, `screens`, `navigation`, and `theme`. Business logic and data flows reside in `repository`, `data`, and `models`. Legacy interoperability and background services remain in `app/src/main/java/com/udacity/astroapp`. Shared resources (drawables, strings, themes) sit in `app/src/main/res`. JVM unit tests live in `app/src/test/java/com/udacity/astroapp`, and instrumentation specs in `app/src/androidTest/java/com/udacity/astroapp`.

## Build, Test, and Development Commands
Run `./gradlew assembleDebug` for a local build and `./gradlew installDebug` to push to an attached device or emulator. `./gradlew testDebugUnitTest` executes JVM tests, while `./gradlew connectedAndroidTest` runs instrumentation suites (ensure an emulator is booted). Use `./gradlew spotlessCheck` before committing and `./gradlew spotlessApply` to auto-format Kotlin and XML sources.

## Coding Style & Naming Conventions
Spotless enforces ktfmt with 4-space indents and 100-character lines; run it whenever you touch Kotlin files. Name composables and classes in UpperCamelCase, functions and properties in lowerCamelCase, and Compose previews with a `Preview` suffix. Keep resource identifiers in lower_snake_case (for example, `ic_mars_rover.png`). Group screen-specific state holders under `ui/screens`, shared UI helpers under `ui/common`, and dependency modules inside `di` to keep feature boundaries clear.

## Testing Guidelines
Write fast JVM tests for view models, repositories, and use cases under `app/src/test`. Instrument UI flows with Espresso or Compose testing APIs in `androidTest`; test classes should end with `Test` or `AndroidTest` and mirror the package of the code under test. Failing tests should gate your PR; add regression coverage whenever you fix a bug or extend a screen.

## Commit & Pull Request Guidelines
Follow the Conventional Commit style shown in history (`feat:`, `fix:`, `chore:`). Keep commits focused, mention ticket IDs in the body, and include before/after screenshots for UI adjustments. PRs must describe scope, test strategy, and any migrations that impact data or navigation.

## Security & Configuration Tips
Never commit real API keys. Replace placeholders in `app/src/main/kotlin/com/udacity/astroapp/utils/Secret.kt` and `app/src/main/res/values/secret_strings.xml` with your local values via untracked files or CI secrets before running the app.
