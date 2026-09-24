# Role Comparison Matrix: Student vs. Lecturer vs. Admin
**Project:** Fyp1 - Smart AI-Driven Lab Management System

---

## 📊 Quick Summary Table (For Presentation Slide)

| Feature / Capability | 🎓 Student Role | 👨‍🏫 Lecturer Role | ⚙️ Faculty Admin Role |
| :--- | :---: | :---: | :---: |
| **View Live Lab Directory & Green/Red Status** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Check Free Windows & AI Recommendations** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Manage Personal Class Schedule ("My Schedule")** | ✅ Yes | ❌ N/A | ❌ N/A |
| **Use AI Retake Assistant (Previous Sem Filter)** | ✅ Yes | ❌ N/A | ❌ N/A |
| **Reserve / Book Lab Slots via Dashboard Cards** | ❌ (View Only) | ✅ Yes | ✅ Yes |
| **Book Labs via Single-Message Chatbot** | ❌ (Guided View) | ✅ Yes | ✅ Yes |
| **Add / Edit Academic Master Class Timetables** | ❌ No | ❌ No | ✅ Yes (`➕ Admin Add`) |
| **Delete Sessions / Clear Timetable Entries** | ❌ No | ❌ No | ✅ Yes (`🗑️ Remove`) |
| **Toggle Lab Maintenance Switches (Realtime DB)** | ❌ No | ❌ No | ✅ Yes (`AVAILABLE / MAINT`) |
| **Broadcast Faculty-Wide Alerts** | ❌ No | ❌ No | ✅ Yes |

---

## 🎯 Detailed Role Breakdown (For Report & Q&A)

### 1. 🎓 Student Role (Focus: Learning, Availability & Retakes)
* **Access Dashboard:** `SecondPageActivity`
* **Core Responsibilities:** Browse lab availability, check free hours, manage personal enrolled timetable, and resolve retake conflicts.
* **Key Privileges:**
  * **View-Only Booking Protection:** Prevents unauthorized student lab reservations.
  * **Personal Timetable Manager:** Add/Delete personal enrolled classes in *"My Schedule"*.
  * **Academic Retake Assistant:** Automatically auto-detects student semester (e.g. Sem 5 for `DIT4A`) and recommends clash-free retake options from previous semesters (Sem 1–4).
  * **AI Chatbot:** Queries lab locations, operating hours, and live availability.

---

### 2. 👨‍🏫 Lecturer Role (Focus: Lab Facility Reservation & Teaching)
* **Access Dashboard:** `SecondPageActivity`
* **Core Responsibilities:** Reserve lab rooms for lectures, practicals, or make-up classes.
* **Key Privileges:**
  * **Direct Lab Reservation:** Can select start/end times and confirm lab bookings directly on dashboard cards or via single-message chatbot (*"book dcn Monday 1pm 4pm"*).
  * **Automatic Name Tagging:** Reservations automatically display the lecturer's name (e.g., `Lecturer: Encik Nizam`) on the Red Occupied Card.
  * **Clash Verification:** Blocked if attempting to book over an official class or existing reservation.

---

### 3. ⚙️ Faculty Admin Role (Focus: Total System Control & Timetable Governance)
* **Access Dashboard:** `AdminActivity` (Admin Command Center)
* **Core Responsibilities:** Manage master academic timetables, set lab maintenance status, and oversee faculty alerts.
* **Key Privileges:**
  * **Master Timetable Manager:** Add new academic class schedules (`Course Code`, `Subject Name`, `Class Name`, `Lecturer Name`, `Day`, `Time`) for all 11 campus labs.
  * **Session Deletion:** Delete specific master timetable entries or user reservations with a single tap.
  * **Maintenance Toggle Switches:** Instantly flip lab status (`AVAILABLE` vs `UNDER MAINTENANCE`) synced in milliseconds via **Firebase Realtime Database**.
  * **Faculty Broadcasting:** Broadcast high-priority alert messages to the faculty feed.
