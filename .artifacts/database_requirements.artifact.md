# Database Integration Requirements: Lab Reservation System (FYP)

This document outlines the technical requirements for the backend/database phase of the project. The UI and logic skeletons are complete; the system now requires a persistent, synchronized data layer.

## 1. Primary Objective
Move the application from local **SharedPreferences** to a centralized **Cloud Database** (Recommended: **Firebase Firestore**) to allow real-time synchronization between multiple users.

---

## 2. Recommended Tech Stack
*   **Database:** Firebase Cloud Firestore (NoSQL) — provides real-time listeners for lab availability.
*   **Authentication:** Firebase Auth — handles secure login and role management.
*   **Storage:** Firebase Storage (Optional) — for lab photos or user profile pictures.

---

## 3. Data Schema (Entities)

### `users` (Collection)
| Field | Type | Description |
| :--- | :--- | :--- |
| `uid` | String | Unique ID from Auth |
| `username` | String | Display name |
| `email` | String | University email |
| `role` | String | "Student", "Lecturer", or "Admin" |
| `joined_date` | Timestamp | Account creation date |

### `labs` (Collection)
| Field | Type | Description |
| :--- | :--- | :--- |
| `lab_id` | String | e.g., "DCN", "NWL" |
| `floor` | String | Ground, 1st, 2nd, 3rd |
| `is_maintenance`| Boolean | If true, block all bookings |
| `description` | String | Specific lab equipment info |

### `bookings` (Collection)
| Field | Type | Description |
| :--- | :--- | :--- |
| `booking_id` | String | Auto-generated |
| `lab_id` | String | Reference to Lab |
| `user_id` | String | Reference to User |
| `user_name` | String | Cached name for quick display |
| `day` | String | Monday - Friday |
| `start_hour` | Number | 24h format (e.g., 8) |
| `end_hour` | Number | 24h format (e.g., 10) |
| `week_offset` | Number | 0 for this week, 1 for next |

---

## 4. Critical Logic Requirements

### A. Atomic Transactions (The "Golden Rule")
When a user clicks "Confirm Booking" in `ThirdPageActivity`, the database must perform an **Atomic Transaction**:
1.  Read all existing bookings for that `lab_id` + `day` + `week_offset`.
2.  Compare hours.
3.  If no overlap: Write the new booking.
4.  If overlap: Return an error to the UI.
*This prevents two students from booking the same lab at the same time.*

### B. Real-time Status Listeners
The `SecondPageActivity` (Dashboard) needs a listener on the `bookings` collection.
*   When a booking is added/deleted in the database, the **Status Dots** (Red/Green) in the UI must update instantly without the user refreshing the app.

---

## 5. Integration Points (UI to DB)
The backend team needs to replace the following "Mock" helpers with Database calls:
*   **Login:** Replace `MainActivity` SharedPreferences check with `FirebaseAuth.signInWithEmailAndPassword`.
*   **Dashboard Status:** Replace `SecondPageActivity.refreshLabAvailability()` with a Firestore Snapshot Listener.
*   **Booking:** Replace `ThirdPageActivity.processBooking()` with a Firestore Transaction.
*   **Assistant:** Replace `ScheduleManager.java` mock data with a query to the `master_timetable` collection.

---

> [!IMPORTANT]
> **Security Rule:** Students should only have `write` access to create bookings for themselves. Only `Admin` roles should be allowed to delete other users' bookings or put a lab into "Maintenance Mode."
