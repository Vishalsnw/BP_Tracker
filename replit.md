# Blood Pressure Tracker Android App

## Overview
A comprehensive Blood Pressure Tracker Android application built with Kotlin and Jetpack Compose. This app helps users monitor their blood pressure readings, view statistics, set reminders, and learn about heart health through informative articles.

## Project Type
**Native Android Application** - This project produces an APK file that runs on Android devices. It is NOT a web application and cannot be run as a web server.

## Building the App
This app should be built using GitHub Actions or a similar CI/CD system with Android SDK installed. The user mentioned they will build via YAML (GitHub Actions workflow).

To build locally (requires Android SDK):
```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room (SQLite)
- **Charts**: Vico Charts Library
- **PDF Export**: iText7
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Project Structure
```
app/src/main/java/com/bptracker/
├── BloodPressureApp.kt       # Application class
├── MainActivity.kt           # Main entry point
├── data/
│   ├── database/             # Room database, DAOs, converters
│   ├── model/                # Data models (BloodPressureReading, Reminder, etc.)
│   └── repository/           # Data repositories
├── di/
│   └── AppModule.kt          # Hilt dependency injection module
├── ui/
│   ├── components/           # Reusable UI components
│   ├── navigation/           # Navigation setup
│   ├── screens/              # App screens (Home, History, Statistics, etc.)
│   └── theme/                # Material theming
└── utils/                    # Utilities (PDF export, notifications)
```

## Features

### Core Features
1. **Blood Pressure Tracking**
   - Record systolic, diastolic, and pulse readings
   - Categorize readings (Normal, Elevated, High Stage 1/2, Crisis)
   - Add tags (Morning, After Meal, etc.)
   - Add notes to readings
   - Mark favorites

2. **History & Statistics**
   - View all readings with filtering
   - 7-day, 30-day, 3-month, 1-year statistics
   - Average, min, max values
   - Category distribution charts

3. **Reminders**
   - Set multiple daily reminders
   - Choose specific days of the week
   - Push notifications

4. **PDF Export**
   - Export reading history to PDF
   - Share reports with healthcare providers

5. **Health Articles**
   - Educational content about blood pressure
   - Topics: Diet, Exercise, Stress, Medications

### Advanced Features
- Dark/Light theme support
- Material You dynamic colors (Android 12+)
- Favorite readings
- Body/arm position tracking
- Reading tags for categorization

## Dependencies
Key dependencies are defined in `app/build.gradle.kts`:
- Jetpack Compose BOM 2023.10.01
- Room 2.6.1
- Hilt 2.48.1
- Vico Charts 1.13.1
- iText7 7.2.5
- WorkManager 2.9.0

## Recent Changes
- Initial project creation (December 2025)
- Complete MVVM architecture setup
- All core screens implemented
- Room database with migrations
- PDF export functionality
- Reminder notifications system
- Fixed crash on startup (December 2025):
  - Fixed Room DAO queries to use String parameters instead of LocalDateTime
  - Updated BloodPressureRepository to convert LocalDateTime to String for database queries
  - Enhanced ProGuard rules for release builds (Compose, Room, Hilt, Vico charts, etc.)
- Major Feature Expansion (December 2025):
  - Added Weight Tracking with BMI calculation and trend analysis
  - Added Blood Glucose Tracking with fasting/post-meal/random types
  - Added Health Goals system with progress tracking and achievements
  - Added AI-powered Insights and Analytics screen
  - Added Alert Management for threshold, crisis, and weekly summary notifications
  - Added CSV Export for weight, glucose, and combined health data
  - Database upgraded to version 3 with new tables: goals, achievements, weight_entries, glucose_entries, alert_settings, insights
  - New repositories: GoalRepository, WeightRepository, GlucoseRepository, AlertRepository, InsightRepository
  - New utilities: InsightGenerator (pattern analysis), AlertManager (notification handling)
  - Updated Settings screen with new Health Tracking and Goals & Insights sections
  - Updated Navigation to support all new screens
- Additional Improvements (December 2025):
  - Added Medication Effectiveness Analysis (MedicationEffectivenessAnalyzer utility)
  - Enhanced PDF Reports with cover page, statistics summary, category distribution, trend analysis, and medication section
  - Added Doctor Report Email Integration with pre-filled email and PDF attachment
  - Added Measurement Quality Score system (MeasurementQualityScorer utility)
  - Updated Medication model with startDate, endDate, and sideEffects fields
  - Database upgraded to version 4 with new medication columns
  - Updated MedicationScreen with start date display and side effects tracking
- Advanced Features Implementation (December 2025):
  - Added Health Connect Integration (HealthConnectManager) - sync with Android Health Connect
  - Added Bluetooth BP Monitor Support (BluetoothBPMonitor) - BLE GATT for compatible devices
  - Added Cloud Backup to Google Drive (CloudBackupManager) - backup/restore via Google Sign-In
  - Added Crisis Response System (CrisisResponseManager) - emergency contacts, SMS alerts, 911 dial
  - New screens: HealthConnectScreen, BluetoothScreen, BackupScreen, EmergencyScreen
  - Updated Settings with Devices & Sync and Safety sections
  - Added new permissions: Bluetooth, Location, Internet, Call, SMS

## User Preferences
- User will build the app via GitHub Actions YAML workflow (to be created upon request)
- No emojis in code or documentation
- Implement ALL suggested improvements, not selective
- Follow Material 3 design patterns consistently
