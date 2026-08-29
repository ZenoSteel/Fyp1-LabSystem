# Fyp1: Lab Reservation System (Pro)

## 🚀 Project Status: UI Phase COMPLETE
The UI, navigation, and core logic skeletons are ready. The project is now transitioning to the **Database & Backend Phase**.

## 🛠 Features Implemented (UI Lead)
*   **Smart AI Chatbot:** Handles lab info, bookings, and typo-tolerant search (via `BotBrain.java`).
*   **Retake Assistant:** Analyzes schedule gaps and provides clash-free suggestions.
*   **Facility Booking:** Optimized spinners with boundary logic to prevent double-bookings.
*   **Dashboard:** Real-time status indicators (ready for Firestore syncing).

## 📁 Key File Map for Backend Team
*   **UI/Layouts:** `app/src/main/res/layout/`
*   **Logic Brain:** `app/src/main/java/com/example/fyp1/BotBrain.java`
*   **Data Handling:** `app/src/main/java/com/example/fyp1/ScheduleManager.java` (Replace mock data here).
*   **Booking Logic:** `app/src/main/java/com/example/fyp1/ThirdPageActivity.java` (Integrate Firestore Transaction in `processBooking`).

## 📋 Database Requirements
Detailed technical requirements for the next phase can be found in the `.artifacts/database_requirements.artifact.md` file.

## ⚙️ How to Run
1. Clone the repo: `git clone https://github.com/ZenoSteel/Fyp1-LabSystem.git`
2. Open in Android Studio (Koala/Ladybug version).
3. Build and run on a physical device or emulator (API 30+ recommended).
