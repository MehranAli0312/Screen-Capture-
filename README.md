# Advanced Screenshot App

## Overview

The Advanced Screenshot App is a feature-rich Android application designed to help users capture, manage, and interact with screenshots. Built using Kotlin, the app follows modern Android development practices and offers a seamless user experience with a variety of functionalities.

## Tech stack & Open-source libraries

- **Minimum SDK level**: 24
- **Language**: 100% Kotlin

### Architecture

- **MVVM Architecture**
  - View: Utilizes View Binding to bind UI components to data sources in your app using a declarative approach.
  - View Model: Manages UI-related data in a lifecycle-conscious way.
  - Model: Represents the data and business logic of the application.

- **Dependencies**
  - **Dependency Injection**: Koin
  - **Asynchronous Programming**: Flow/LiveData
  - **Navigation**: Jetpack Navigation
  - **Design**: Material Design

## Features

### Foreground Service and Notification
- **Foreground Service:** Runs on app startup to ensure continuous availability.
- **Notification:** Displays a persistent notification with two actionable buttons:
  - **Screenshot:** Closes the notification tray, captures a screenshot of the current screen, and saves it to the mobile gallery.
  - **Cancel:** Closes the application and ensures no background service is left running.

### Screenshot Functionality
- **Capture Screenshots:** Allows users to take screenshots of the currently opened mobile window.
- **Save to Gallery:** Automatically saves each captured screenshot to the device's gallery.

### RecyclerView for Screenshots
- **Display Screenshots:** Uses RecyclerView to list all captured screenshots on the main screen.
- **Item Actions:** Each screenshot item includes a button to open a menu dialog with options to:
  - **Delete:** Remove the screenshot from the gallery and RecyclerView.
  - **Share:** Share the screenshot through various apps.
  - **Open:** View the screenshot in a new activity.

### RecyclerView Updates
- **Dynamic Updates:** Utilizes best practices for updating the RecyclerView data when a screenshot is saved or deleted.

### Google AdMob Integration
- **Interstitial Ads:** Displays interstitial ads when any screenshot item is clicked, enhancing user engagement.
- **Ad Callbacks:** Properly handles interstitial ad callbacks to ensure a smooth user experience.

### Smart Native Ads
- **Ad Placement:** Integrates smart native ads into the RecyclerView, with an ad appearing every third row:
  - **First and Second Rows:** Contain three screenshot image items each.
  - **Third Row:** Displays a smart native ad.

### Swipe Refresh Layout
- **Refresh Data:** Incorporates a swipe-to-refresh layout with RecyclerView to provide a seamless and efficient interface.
- **LiveData and Coroutines:** Enhances the user experience with LiveData for data observation and coroutines for asynchronous operations.

## Screenshots


## Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/MehranAli0312/Screen-Capture-.git

### Contribution
Please contribute to this list! We need your support to keep this list up-to-date. If you find any incorrect data, feel free to fix it by opening a pull request or creating an issue.
