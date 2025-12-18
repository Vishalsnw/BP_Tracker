# Blood Pressure Tracker - Replit Import

## Project Overview
A comprehensive Android/Kotlin blood pressure tracking application with advanced features including cloud backup, Bluetooth device integration, Health Connect sync, medication tracking, and emergency response capabilities.

## Technology Stack
- **Language**: Kotlin
- **Framework**: Android (API 26-34)
- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room Database
- **Dependency Injection**: Hilt
- **Build System**: Gradle 8.2

## Build Requirements
- Java 17+ (GraalVM 22.3 installed)
- Gradle 8.2

## Features Implemented (34/38)
✅ Phase 1: Quick Wins (5/5) - CSV Export, Threshold Alerts, Weekly Notifications, Time-of-Day Analysis, Data Insights
✅ Phase 2: Core Features (6/6) - Health Connect, Goal Tracking, Smart Alerts, Medication Analysis, Enhanced Reports, Doctor Email
✅ Phase 3: Advanced Features (6/6) - Bluetooth Monitor Sync, Cloud Backup, Weight Tracking, Glucose Tracking, AI Insights, Quality Scoring
✅ Phase 4: Premium Features (17/18) - WearOS pending, OCR pending, Family Sharing pending, Multi-language pending

## Pending Premium Features
- [ ] WearOS Companion App
- [ ] OCR Reading Capture
- [ ] Family Sharing/Caregiver Access
- [ ] Multi-language Support

## How to Build
```bash
./gradlew build
./gradlew assembleDebug  # For debug APK
./gradlew assembleRelease  # For release APK
```

## Import Status
✅ Java toolchain installed and verified
✅ All dependencies configured in build.gradle.kts
✅ Project structure verified
✅ Ready for development/deployment

## Recent Changes (December 2025)
- **Professional UI/UX Redesign**: Implemented healthcare-grade design inspired by top BP apps (SmartBP, Heartify, Cardiogram)
  - New calming blue/teal color palette (#0891B2) replacing red for better healthcare aesthetics
  - Modern gradient hero cards with floating decorative elements
  - Improved typography with better hierarchy and visual weight
  - Grouped settings sections with color-coded icons
  - Enhanced card designs with subtle shadows and rounded corners
  - Updated blood pressure category colors for better accessibility
- Fixed Bluetooth permission flow with proper ActivityResult launcher - auto-starts scan after grant
- Improved Google Sign-In error handling with ApiException status code mapping
- Added SMS permission request for Crisis Response with graceful denial handling
- Fixed HorizontalDivider → Divider for Material3 compatibility (8 instances)
- Resolved META-INF/DEPENDENCIES resource merge conflict in build.gradle.kts
- Removed cloud backup feature (Google Sign-In Error 10)

## Design System
- **Primary Color**: Healthcare Blue (#0891B2)
- **Color Palette**: Calming blues, teals, and greens - professional healthcare aesthetic
- **BP Category Colors**: Green (Normal), Amber (Elevated), Orange (High Stage 1), Red (High Stage 2), Purple (Crisis)
- **Data Visualization**: Pink (Systolic), Blue (Diastolic), Green (Pulse)
- **Gradients**: Smooth blue-to-cyan gradients for hero cards and accents
