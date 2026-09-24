# 6th InfoTeX Presentation & Pitching Master Strategy
**Project:** Fyp1 - Smart AI-Driven Lab Management System
**Presentation Time:** 7 Minutes Pitch + Live Demo
**Q&A Time:** 3 Minutes (Industry Evaluator)

---

## 1. Alignment with 6 Core Evaluation Rubrics

| Rubric Criteria | Project Proof Point (Fyp1 System) |
| :--- | :--- |
| **01 Problem / Need** | Manual lab booking leads to double-bookings, empty unutilized rooms, and scheduling clashes for students retaking failed subjects. |
| **02 Innovation (Novelty)** | **3 Key Innovations:** <br>1. AI Clash-Free Gap Analyzer for retake students.<br>2. Typo-tolerant NLP Chatbot (`BotBrain.java`) handling natural inputs like "book lab1 Thursday 1pm 4pm".<br>3. Dual Cloud Database Architecture (Firestore + Realtime DB) for instant status sync. |
| **03 Solution** | Role-Based Access Control (RBAC) Android app separating Student, Lecturer, and Admin workflows with atomic conflict prevention. |
| **04 Technology** | **Current Tech Stack:** Android Studio Java, Firebase Authentication, Cloud Firestore, Firebase Realtime DB, Google ML Kit OCR. |
| **05 Prototype / System** | **100% Functional Live Prototype:** Real-time database sync on physical device without static simulations. |
| **06 Impact / Benefit** | Saves 80% time in lab coordination, eliminates double-bookings, and maximizes TVET lab facility utilization. |

---

## 2. 7-Minute Pitching Script & Live Demo Breakdown

### Minute 0:00 - 1:00 | Problem & Need (Masalah & Keperluan)
> *"Assalamualaikum and good day to the panel. In polytechnic TVET education, lab facilities like DCN, NWL, and Multimedia Labs are critical. However, manual booking causes two major problems: first, **frequent double-booking conflicts** between lecturers, and second, **severe scheduling clashes for students trying to retake failed subjects**."*

### Minute 1:00 - 2:30 | Solution & Technology (Solusi & Teknologi)
> *"To solve this, we built **Fyp1 Lab Management System**—a smart Android app powered by Cloud Firebase and AI. We used a **Dual-Database Architecture**: Cloud Firestore for structured bookings and Firebase Realtime DB for instant millisecond maintenance toggles. We also integrated Google ML Kit OCR to allow instant timetable scanning."*

### Minute 2:30 - 5:30 | LIVE DEMO (Ujian Langsung / Live Demo)
*(Demonstrate these 3 live flows directly on your phone)*:
1. **Student Retake Assistant Demo:** Select *"DIT4A (Sem 5)"*, tap *"Analyze for Gaps"*. Show the panel how the system detects previous semester subjects (Sem 1-4) and recommends clash-free retake slots.
2. **AI Booking Assistant (Single-Message NLP):** Type `"book dcn Monday 1pm 4pm"`. Show how the bot detects lab, day, and time in one sentence and performs an atomic clash check before saving to Cloud Firestore!
3. **Admin Command Center:** Log in as `admin`/`123`. Toggle DCN switch to *"Under Maintenance"* and tap *"Manage Lab Timetables"* to add/delete class schedules.

### Minute 5:30 - 7:00 | Impact, Cost & Benefit (Impak & Value)
> *"This system transforms manual paper coordination into an automated TVET smart campus solution, saving hours of administration time and guaranteeing 100% conflict-free lab usage."*

---

## 3. Anticipated Panel Q&A (3 Minutes - Sesi Soal Jawab)

### Q1: "Bagaimana sistem anda memastikan tiada dua orang menempah makmal yang sama pada masa yang sama?" (Concurrency / Double-Booking)
* **Your Answer:** *"Sistem kami menggunakan **Atomic Overlap Verification** di peringkat kod (`BookingUtils.isOverlap`) dan Firebase Transactions. Sebelum sebarang tempahan disimpan, sistem menyemak kedua-dua tempahan pengguna dan jadual waktu akademik rasmi. Jika terdapat pertindihan walau 1 minit, sistem secara automatik menyekat tempahan tersebut."*

### Q2: "Apakah kebaharuan (novelty) sistem ini berbanding sistem sedia ada?" (Innovation)
* **Your Answer:** *"Kebaharuan utama kami ialah **AI Academic Conflict Resolver** untuk pelajar yang mengulang subjek. Sistem secara automatik menyemak jadual peribadi pelajar dan mencadangkan slot makmal yang bebas daripada pertindihan. Selain itu, **AI Chatbot** kami menyokong pemprosesan bahasa semula jadi sebaris seperti 'book dcn Monday 1pm 4pm'."*

### Q3: "Adakah sistem ini berfungsi secara langsung atau sekadar simulasi?" (Live Prototype)
* **Your Answer:** *"Ia 100% prototaip berfungsi secara langsung. Semua data disimpan secara masa nyata (real-time) dalam Google Firebase Cloud Firestore dan Realtime Database di pelayan Singapura."*
