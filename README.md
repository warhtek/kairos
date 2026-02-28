# kairos

## Project structure

```
kairos/
├── androidApp/                   # Android app module
│   └── src/main/java             # Production code
│   └── src/test/java             # Unit tests
├── core/
│   ├── domain/                   # Business logic (pure Kotlin)
│   │   └── src/main/java/        # Standard Android source code
│   └── data/                     # Data layer
│       └── src/main/java/        # Standard Android source code
│       └── src/test/java/        # Unit tests
├── .gitignore
├── libs.versions.toml
└── settings.gradle.kts
```

## How to build

### ADB Install Build

```bash
git clone https://github.com/wearemobi/kairos.git
cd kairos
gradle installDebug
```

> The app will be installed on all devices accessible to `adb`.

### Developer Build

```bash
gradle assembleDebug
```

## How to run tests and coverage

```bash
# Run unit tests and generate coverage report (using Kover)
gradle testDebugUnitTest koverHtmlReportDebug

# Open Kover HTML report for `:app` 
open androidApp/build/reports/kover/htmlDebug/index.html

# Open Kover HTML report for `:core:data` 
open core/data/build/reports/kover/htmlDebug/index.html
```

## Requirements

- **Gradle 9.1+** installed on your system
- **Android Gradle Plugin 9.0.1** (defined in `app/build.gradle.kts`)
- Java 17+ (`JAVA_HOME` configured)
- Android SDK with API 36 (`ANDROID_HOME` configured)

## Notes

- No Gradle wrapper is included — the project expects you to have Gradle installed globally. This keeps the repository even smaller and lets you use your preferred Gradle version.
- Using native Android themes (`android:Theme.Material`) means no AppCompat dependency required.
- Colors are handled entirely by Compose — XML themes only control the ActionBar.
- Version catalog TOML file makes it easy to update dependencies.
- **Tests use Robolectric** - runs in seconds without emulators
- **Coverage uses Kover** - simpler than JaCoCo, works out of the box
- AGP 9.0.1 works best with **Android Studio Panda 1 | 2025.3.1 Patch 1** or newer**, but you can use any IDE that supports Gradle builds.
