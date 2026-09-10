# ProntoTix Android

Native Android application for **ProntoTix**, a field operations management system designed to help manage drivers, work shifts, service tickets, and field activities.

The application is developed with **Kotlin and Jetpack Compose** and communicates with a backend built with **Swift and Vapor** through REST APIs.

## Tech Stack

* Kotlin
* Jetpack Compose
* Material 3
* Android Studio
* Gradle Kotlin DSL
* REST APIs

## Main Features

* User authentication
* Service ticket and field activity management
* Ticket detail and status management
* Driver work shift management
* Start and end work shifts
* Integration with the ProntoTix REST API
* Modern UI built with Jetpack Compose

## Screenshots

![ProntoTix Android](Screenshot_20260803_201046.png)

## Backend

The application communicates with the **ProntoTix Backend**, developed with Swift and Vapor.

Backend repository:

https://github.com/luisvicente2021/ProntoTixBackend

The backend provides authentication, driver management, work shifts, location tracking, delivery reports, tracking events, and service ticket APIs.

## Running the Project

### Requirements

* Android Studio
* JDK 17
* Android SDK

Clone the repository:

```bash
git clone https://github.com/luisvicente2021/ProntoTix-Android.git
cd ProntoTix-Android
```

Open the project in Android Studio and wait for Gradle Sync to finish.

You can also build the project from the terminal:

```bash
./gradlew assembleDebug
```

## About the Project

ProntoTix originally started as a ticket management system and evolved into a broader solution for managing and tracking field staff operations.

This project has allowed me to expand my mobile development experience from iOS into native Android development, working with Kotlin, Jetpack Compose, REST API integration, application state, and mobile UI development.

The Android application and Swift/Vapor backend are developed as complementary parts of the same system.

## Author

**Luis Angel Vicente Robles**

Software Engineer | Mobile Developer
Swift • iOS • Kotlin • Android

