# FINAL YEAR PROJECT PROPOSAL / EXECUTIVE REPORT

**Project Title:** AI-Assisted Facility Tracker and Reservation System
**Institution:** Jabatan Teknologi Maklumat dan Komunikasi (JTMK), Politeknik Kuching Sarawak
**Developers:**
1. Muhammad Zaim Bin Mahathir
2. Muhammad Firdaus Bin Abdul Razak
3. Mohammad Syabil Adha Bin Saithu Mohammad
**Supervisor:** Dr. Zakir Hussain Bin Ibrahim

---

## ABSTRACT / EXECUTIVE SUMMARY
Managing campus laboratory facilities using traditional, paper-based, or decentralized methods creates severe administrative bottlenecks, leading to double-booked rooms, wasted facility uptime, and scheduling clashes for students. To address this, the **AI-Assisted Facility Tracker and Reservation System** was developed. This project is a cloud-connected mobile application built on Android Studio (Java) and Google Firebase. It integrates custom artificial intelligence logic—including an NLP (Natural Language Processing) Chatbot, an Academic Retake Assistant, and a Live Gap Engine—to automate lab tracking. By deploying a Dual-Database Architecture (Cloud Firestore and Realtime Database), the system provides zero-latency room status updates across all campus devices, enforcing 100% clash-free operations and proactive facility maintenance.

---

## CHAPTER 1: INTRODUCTION

### 1.1 Project Background
In TVET institutions like Politeknik Kuching Sarawak, computer laboratories are critical resources. Currently, verifying lab availability requires physical inspection ("the blind walk") or manual cross-referencing of paper timetables. This manual approach reduces administrative efficiency and increases the risk of scheduling overlaps, especially during ad-hoc replacement classes.

### 1.2 Problem Statement
1. **Lack of Real-Time Visibility:** Students and lecturers lack an instant, floor-by-floor visual mapping of active lab occupancy, causing wasted time walking to locked or occupied rooms.
2. **Scheduling Overlaps & Conflicts:** Manual ad-hoc bookings lead to double-booked rooms, disrupting officially scheduled classes.
3. **Complex Retake Enrollment:** Students repeating failed subjects face immense difficulty manually matching open lab sessions with their individual conflict-free timetables.
4. **Unstructured Maintenance Reporting:** Lab hardware/facility issues (e.g., broken PCs, ceiling leaks) are reported informally without systematic severity prioritization, delaying urgent repairs.

### 1.3 Project Objectives
1. To develop an interactive mobile application displaying real-time availability for all **11 JTMK computer labs**.
2. To integrate an AI recommendation engine that detects continuous free time windows and enforces atomic overlap prevention.
3. To provide an **AI Retake Assistant** that automatically scans master timetables and suggests clash-free course slots for repeating students.
4. To implement an **Admin Command Center** featuring an AI issue classifier that ranks facility repairs by urgency (High/Medium/Low) for rapid IT response.

### 1.4 Project Scope & Target Users
* **Target Environment:** 11 JTMK Laboratories (DCN, NWL, DNS, SAD Lab, Network Lab, PGL1, PGL2, CSD, HYP, APL, MML).
* **Target Users:**
  * **Students:** View-only access, personal timetable management, and Retake Assistant access.
  * **Lecturers:** Lab reservation capabilities, chatbot booking, and AI schedule recommendations.
  * **Faculty Admins:** Master timetable management, emergency broadcasting, and real-time maintenance overrides.

---

## CHAPTER 2: METHODOLOGY & TECHNOLOGY STACK

### 2.1 System Architecture
The system utilizes a **Distributed Dual-Cloud Architecture** to separate heavy relational data from instantaneous status updates:
* **Cloud Firestore:** Manages structural data including User Profiles, Master Timetables, and User Bookings.
* **Firebase Realtime Database:** Acts as the high-speed backbone for live status dots (Green/Red), Admin Maintenance Switches, and Faculty-Wide Emergency Broadcasts.

### 2.2 Technology Stack
* **Frontend Development:** Android Studio, Java, XML, Material Design 3 UI/UX principles.
* **Backend Services:** Google Firebase Authentication, Firebase Cloud Firestore, Firebase Realtime Database.
* **Smart Logic / AI:**
  * Custom Java Fuzzy-Logic Engine (`BotBrain.java`) for NLP intent recognition.
  * Time-Matrix Gap Calculation (`BookingUtils.java`).
* **Computer Vision:** Google ML Kit OCR (Optical Character Recognition) for timetable image parsing.

---

## CHAPTER 3: SYSTEM FEATURES & IMPLEMENTATION

### 3.1 Hierarchical Role-Based Access Control (RBAC)
The application dynamically routes users based on their authenticated role:
* **Student Module:** Read-only access to lab status to prevent unauthorized bookings. Includes a "My Schedule" manager where students can import classes directly from the master database.
* **Lecturer Module:** Full access to the AI Chatbot and Dashboard reservation tools to book labs instantly.
* **Admin Module:** Access to the "Admin Command Center" to oversee system analytics, edit the master academic timetable, and toggle lab hardware statuses.

### 3.2 Smart AI Capabilities
1. **AI Retake Assistant:** The system automatically detects a student's current semester (e.g., Semester 5) and cross-references their enrolled timetable against all previous semester courses (Sem 1-4). It generates a 100% clash-free list of retake options.
2. **Single-Message NLP Chatbot:** Users can type natural sentences (e.g., *"book DNS Monday 1pm 4pm"*). The chatbot automatically extracts the facility, day, and time, performs a 3-way clash verification, and registers the booking atomically.
3. **Atomic Overlap Prevention:** Any booking attempt is programmatically verified against both *Existing User Bookings* and the *Official Master Timetable*. If a lab is under maintenance or occupied, the transaction is rejected, guaranteeing zero double-bookings.

### 3.3 Real-Time Maintenance & Broadcasting
Administrators can flip a switch to mark a lab as "Under Maintenance". In milliseconds, this data passes through the Realtime Database and updates the lab's status dot to **RED** on every active user's device. Admins can also push high-priority alert banners (e.g., *"Power Outage"*).

---

## CHAPTER 4: IMPACT & COMMERCIAL VALUE (TVET FOCUS)

### 4.1 Digital Transformation & Time Efficiency
The application successfully digitizes a highly manual process. The **1-Tap Auto-Fill** and Chatbot features reduce the administrative time required to coordinate a replacement class from several minutes (or hours of finding an empty room) down to less than 10 seconds.

### 4.2 Proactive Campus Preservation
By categorizing facility damage reports into High, Medium, and Low priorities, the IT department can address critical hazards (such as server room leaks) proactively, saving thousands of Ringgit in potential hardware damage.

### 4.3 Academic Progression
The AI Retake Assistant eliminates the schedule friction repeating students face. By guaranteeing clash-free retake options, students can enroll with confidence, thereby improving overall faculty graduation rates and supporting TVET academic excellence.

---

## CONCLUSION
The **AI-Assisted Facility Tracker and Reservation System** achieves all stated objectives by delivering a highly scalable, real-time, and intelligent mobile platform. By leveraging Google Firebase and custom Java-based smart logic, the project proves that AI and Cloud technologies can be seamlessly integrated into campus administration to eliminate human error, prevent scheduling conflicts, and modernize TVET facility management.
