# GateCrash – Airport Layover Assistance Application

## Project Overview

GateCrash is an Android application designed to help travellers manage their airport layovers more conveniently. It provides a single platform where users can manage flight information, explore airport facilities, maintain travel checklists, and set reminders for important travel activities.

The application is developed using Java and XML in Android Studio. It uses SQLite for local data storage and integrates Google Maps and an external flight-data API to support flight information and airport navigation features.

## Problem Statement

Passengers travelling through unfamiliar airports may find it difficult to locate facilities, organize their travel activities, keep track of flight information, and remember important tasks during layovers.

GateCrash aims to simplify this experience by combining flight management, airport facility exploration, checklists, and reminders in one mobile application.

## Objectives

- Provide a user-friendly interface for managing flight-related information.
- Allow users to register and log in to the application.
- Enable users to add and view flight details.
- Help users explore airport facilities using map-based features.
- Provide checklists for organizing travel essentials and activities.
- Allow users to schedule reminders for important travel tasks.
- Store relevant information locally using SQLite.

## Key Features

### 1. User Authentication
Provides registration and login screens for accessing the application.

### 2. Flight Management
Allows users to enter flight details and view information through the flight dashboard.

### 3. Flight Information
Uses an external flight-data service and predefined fallback information to retrieve or display flight details, depending on data availability and API configuration.

### 4. Airport Explorer
Helps users explore airport facilities through a dedicated interface.

### 5. Map Integration
Uses Google Maps to display map information, airport facility markers, and simulated route guidance. The application does not provide verified indoor turn-by-turn navigation.

### 6. Travel Checklist
Helps users organize important travel tasks and items.

### 7. Reminder System
Allows users to schedule and cancel reminders using Android's alarm functionality. The available reminder intervals are based on the application's implementation.

### 8. Local Database
Uses SQLite to store relevant information such as user records, trips, checklist data, and reminder records.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application logic |
| XML | User interface layouts |
| Android Studio | Development environment |
| SQLite | Local data storage |
| Google Maps SDK | Map display and markers |
| Android Location Services | Location-related functionality, where configured |
| AviationStack API | External flight-data retrieval, subject to API configuration |
| AlarmManager | Scheduling reminders |
| BroadcastReceiver | Handling scheduled alarm broadcasts |

## Project Structure

The main Java files and their responsibilities are listed below.

| File | Purpose |
|---|---|
| `LoginActivity.java` | Handles the login interface and related logic |
| `RegisterActivity.java` | Handles user registration |
| `HomeActivity.java` | Displays the home screen and navigation options |
| `AddFlightActivity.java` | Accepts flight details |
| `FlightDashboardActivity.java` | Displays the flight dashboard |
| `LiveFlightActivity.java` | Displays available flight information |
| `LiveFlightTrackerService.java` | Handles external flight-data retrieval and related logic |
| `FlightResolver.java` | Resolves flight details and provides fallback data |
| `PlacesManager.java` | Handles airport place-related information |
| `AirportDataRepository.java` | Provides airport facility data, including predefined data |
| `AirportFacility.java` | Represents an airport facility as a data model |
| `MapActivity.java` | Displays maps, markers, and simulated route guidance |
| `AirportExplorerActivity.java` | Provides the airport exploration interface |
| `DatabaseHelper.java` | Creates and manages the SQLite database |
| `ChecklistActivity.java` | Displays and manages checklist items |
| `FlightInfo.java` | Represents flight information as a data model |
| `ReminderActivity.java` | Manages reminder scheduling and cancellation |
| `AlarmReceiver.java` | Receives scheduled alarm broadcasts |

The project may also contain XML layout files, Android resources, configuration files, and Gradle build files.

## Requirements

- Android Studio
- Android SDK compatible with the project's configuration
- Java development support
- An Android emulator or compatible Android device
- Google Maps API key, if required by the configured map functionality
- AviationStack API key and suitable API configuration for external flight-data retrieval

## How to Run the Project

1. Download or clone the repository.
2. Open Android Studio.
3. Select **Open** and choose the extracted project folder.
4. Allow Gradle to sync and resolve the project dependencies.
5. Configure the required API keys according to the project's configuration.
6. Check the Android manifest for the required permissions and component declarations.
7. Run the application on an emulator or a compatible Android device.

### API Key and Security Note

Do not upload private API keys, passwords, or other credentials to a public GitHub repository. Configure API keys through an appropriate local configuration method and restrict the keys according to the relevant provider's security recommendations.

## Permissions

Depending on the enabled features and Android version, the application may require permissions or system access for:

- Internet access for external flight-data retrieval and map services.
- Location access for location-related features, where implemented.
- Notifications for reminder alerts.
- Exact alarm scheduling on supported Android versions, where required.

Users may need to grant permissions or enable the appropriate system settings for certain features to work.

## Current Limitations

- Flight information depends on API availability, API configuration, and the data provided by the external service.
- Predefined fallback data may be used when live flight information is unavailable.
- Airport facility information includes predefined data and may not represent a complete or currently verified airport directory.
- Map markers and simulated routes should not be treated as verified indoor navigation instructions.
- Reminder timing is based on the selected interval from the time the reminder is set, rather than automatically calculating the interval from the actual flight departure time.
- Notifications and alarm behaviour depend on Android permissions, system settings, and the implementation of the alarm receiver.

## Future Enhancements

- Add a more complete and verified airport facility database.
- Improve flight-data handling and error messages when an API is unavailable.
- Integrate flight-departure-based reminder calculations.
- Improve route guidance and provide more detailed accessibility information.
- Add synchronization and backup options for user data.
- Improve the user interface and accessibility of the application.
- Expand testing across different Android versions and devices.


## Conclusion

GateCrash aims to make airport layovers easier to manage by bringing flight information, airport facility exploration, travel checklists, and reminders into one Android application. The project demonstrates the use of Android development, local database management, API integration, maps, and background alarm functionality in a practical travel-related application.
