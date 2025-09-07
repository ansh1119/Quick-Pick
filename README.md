QuickPick

QuickPick is a modern, scalable application designed to deliver a seamless user experience while adhering to Clean Architecture and the MVVM (Model-View-ViewModel) pattern. Built with a focus on maintainability and future scalability, the project demonstrates best practices in Android development and cross-platform readiness.

✨ Features

Clean Architecture + MVVM for clear separation of concerns and testability

Kotlin Coroutines for smooth and efficient asynchronous programming

Firebase Integration for authentication, real-time data sync, and analytics

Razorpay Payment Gateway integration (implementation complete, pending production release after discussions on routes, minimum payments, and commission charges)

Future-ready migration to Kotlin Multiplatform (KMP) for cross-platform support

🏗️ Tech Stack

Kotlin (Primary language)

Android Jetpack Components (ViewModel, LiveData/StateFlow, Navigation, etc.)

Coroutines + Flow for async and reactive programming

Firebase (Auth, Firestore, Analytics)

Razorpay SDK for payments

Clean Architecture layered structure

🚧 Current Status

Core functionality is stable and live

Razorpay integration is implemented but not yet pushed to production (awaiting finalization of routes, minimum payments, and commission charges with Razorpay team)

Migration to Kotlin Multiplatform (KMP) is in progress to extend support beyond Android

📂 Project Structure
QuickPick/
 ├── data/        # Repositories, Data sources, API & DB handling
 ├── domain/      # Use cases, Business logic
 ├── ui/          # ViewModels, UI screens (Jetpack Compose / XML)
 └── di/          # Dependency Injection setup

🚀 Getting Started

Clone the repository:

git clone https://github.com/your-username/QuickPick.git


Open in Android Studio (latest version recommended).

Add your own Firebase configuration file (google-services.json) under app/.

(Optional) Set up Razorpay sandbox credentials for payments testing.
