# Wajebaat Tracker

A native Android app to track monetary entries and calculate Wajebaat (1/5 of amount).

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with API 34

### Steps
1. Open Android Studio
2. Select **File → Open** and choose the `WajebaatTracker` folder
3. Wait for Gradle sync to complete (may take several minutes first time)
4. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Calculation Logic
- **Wajebaat** = Amount ÷ 5
- **Money Left** = Amount - Wajebaat
- Example: ₹100 → Wajebaat ₹20 → Money Left ₹80

## Features
- ✅ Add entries with automatic calculation
- ✅ View all records sorted by latest first
- ✅ Edit any entry (recalculates automatically)
- ✅ Delete individual entries with confirmation
- ✅ Summary with real-time totals
- ✅ Dark/Light mode toggle (persisted)
- ✅ Export to CSV
- ✅ Export to Excel (.xlsx) with formatting
- ✅ Delete all data with confirmation
- ✅ Offline-first with Room (SQLite)
- ✅ Supports 5000+ entries for 2+ years

## Architecture
- MVVM + Repository pattern
- Room database
- LiveData for reactive UI
- DataStore for preferences
- Apache POI for Excel export
- Material 3 design
