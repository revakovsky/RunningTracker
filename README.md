# Running Tracker

An app that tracks your runs, displays your routes on a map in real time, and keeps you connected
across all your devices. The app also has the ability to connect wearable devices such as smart
watches and manage your run status from either your phone or watch at the same time.

---

### Requirements and Limitations

#### Minimum Android Version:
- Mobile app: Android 8.0 (API level 26)
- Wear OS app: Android 11.0 (API level 30)

#### Project Launch:
This project requires a private API key, which is not included in the public repository. As a
result, the code is available for review, but the app cannot be executed without the necessary
credentials.

### Basic Architecture
- Multi-module project
- MVI (Model-View-Intent) pattern
- Offline-first app

### Project Configuration
- Convention Gradle plugins
- Secrets Gradle Plugin for Android
- Android Splash Screen API
- Nested type-safe navigation
- Implementing a dynamic loaded Analytics module in the app
- Work Manager to sync data
- Availability and support of the Wear OS app

### Authentication
- Authentication process using bearer tokens for access and refresh mechanisms
- Encrypted SharedPreferences to save bearer tokens and maintain login state

### Libraries and Frameworks
- Room Database for offline storage
- Koin for dependency injection
- Ktor HttpClient with its utility functions
- Google Maps api

### Features and Functionality
- Custom Result class to process Http requests and errors
- Kotlin Flows to transfer data
- Work with focusable states and bringIntoButtonViewRequester
- Multiple permission handling
- Tracking location and drawing a running path
- Foreground Service with a Pending intent to track the run in background and reopen a current
  active run screen

### Wear OS Integration
- Discovering paired devices (phone and watch)
- Share info and actions between paired devices by using Wearable Messaging Client
- Ambient mode for the wearable device

### Testing
- Convention Gradle plugin for the testing
- Sharable test utility in a multi-module project for Unit tests and Instrumented Integration tests
- Unit and Instrumented Integration tests
