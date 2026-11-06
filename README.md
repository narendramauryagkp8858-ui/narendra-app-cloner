# Narendra App Cloner

Android Studio / Kotlin starter project with a GitHub Actions APK workflow.

## Current features
- Lists launchable apps visible to Android's package-visibility rules.
- Launches the selected original app.
- Saves and removes local profile records for each app.
- GitHub Actions builds a debug APK and publishes it as an artifact.

## Important limitation
This starter app does **not** create a second independently installed copy of another Android app, nor does it isolate that app's private data. Ordinary Android apps are sandboxed. Real app cloning generally requires Android-managed work profiles / a device policy controller, OEM-supported dual-app features, or app-specific multi-account support. A saved profile record in this MVP is not a cloned app instance.

## Build
Open this folder in Android Studio with JDK 17 and Android SDK 35, or push it to GitHub and run the `Build Narendra App Cloner APK` workflow under Actions. Download the `Narendra-App-Cloner-debug` artifact and extract the APK.
