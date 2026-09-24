# 6th InfoTeX Presentation Slide Deck Outline
**Project:** Fyp1 - Smart AI-Driven Lab Management System
**Format:** Concise Bullet Points for PowerPoint / Canva Slides

---

## Slide 1: Title Slide (Slaid Tajuk)
### **Pembangunan Sistem Pengurusan & Tempahan Makmal Pintar Berpacukan AI (Fyp1 Lab Management System)**
* **Kategori:** Teknologi Maklumat & TVET Smart Campus
* **Jabatan:** Jabatan Teknologi Maklumat & Komunikasi (JTMK)
* **Ahli Kumpulan:** [Nama Ahli Kumpulan & Penyelia]

---

## Slide 2: 01 Problem / Need (Penyataan Masalah & Keperluan)
### **Isi Utama (Bullet Points):**
* ❌ **Isu Pertindihan Masa (Double-Booking):** Pengurusan manual menyebabkan bentrokan jadual antara pensyarah dan pelajar.
* ❌ **Masalah Pelajar Mengulang (Retake Clashes):** Pelajar yang mengulang subjek sukar mencari slot makmal yang bebas pertindihan.
* ❌ **Pemanfaatan Makmal Kurang Efisien:** Makmal kosong tanpa maklumat ketersediaan masa nyata (real-time).
* 🎯 **Keperluan:** Sistem tempahan automatik berpusat dengan pengesahan pertindihan masa nyata dan carian slot bebas AI.

---

## Slide 3: 02 Innovation / Novelty (Nilai Kebaharuan & Keaslian Idea)
### **Isi Utama (Bullet Points):**
* 💡 **1. AI Academic Conflict Resolver:** Algoritma carian slot automatik untuk pelajar mengulang subjek mengikut semester terdahulu (Sem 1–4).
* 💡 **2. Pemprosesan Bahasa Semula Jadi (NLP) Chatbot:** Bot pemprosesan sebaris (*"book dcn Monday 1pm 4pm"*) yang bertoleransi ralat ejaan (fuzzy search).
* 💡 **3. Senibina Dual-Cloud Database:** Gabungan Cloud Firestore dan Firebase Realtime DB untuk penyelarasan status makmal dalam milisaat.
* 💡 **4. AI OCR Timetable Importer:** Pengecaman teks berasaskan Google ML Kit untuk membaca jadual waktu terus daripada imej jadual.

---

## Slide 4: 03 Solution (Penyelesaian Logik & Praktikal)
### **Isi Utama (Bullet Points):**
* 🔒 **Kawalan Capaian Berasaskan Peranan (RBAC):**
  * **Pelajar:** Carian ketersediaan, jadual peribadi & pembantu mengulang subjek (view-only booking).
  * **Pensyarah:** Tempahan slot makmal melalui kad / chatbot AI.
  * **Admin:** Kawalan pusat makmal (*Under Maintenance* toggle) & pengurusan jadual waktu rasmi (*Add/Delete Timetable*).
* 🛡️ **Peraturan Pengesahan Atomik (Atomic Conflict Prevention):** Menyekat tempahan bertindih secara automatik dengan amaran *"Clash Detected"*.

---

## Slide 5: 04 Technology (Teknologi Maklumat Terkini & Piawaian)
### **Isi Utama (Bullet Points):**
* 📱 **Pembangunan Asal Android:** Android Studio (Java) berasaskan piawaian Material Design 3.
* ☁️ **Google Firebase Cloud Services:**
  * **Firebase Authentication:** Pengesahan akaun selamat berasaskan awan.
  * **Cloud Firestore:** Penyimpanan data jadual & tempahan terstruktur.
  * **Firebase Realtime DB:** Penyelarasan status selenggara makmal masa nyata (Singapore Node).
* 🤖 **Google ML Kit Vision API:** Pengecaman OCR berasaskan peranti untuk imej/dokumen jadual.

---

## Slide 6: 05 Prototype / System (Prototaip Berfungsi & Kestabilan)
### **Isi Utama (Bullet Points):**
* ⚡ **100% Prototaip Masa Nyata (Live Working Prototype):** Berfungsi secara terus pada peranti fizikal Android tanpa simulasi rekaan.
* 🔄 **Penyelarasan Awan Terus (Real-Time Sync):** Pertukaran status makmal di peranti A serta-merta dikemas kini pada peranti B.
* 🛡️ **Sokongan Mod Luar Talian (Offline Fallback):** Sistem kekal stabil menggunakan storan tempatan sekiranya liputan rangkaian terputus.

---

## Slide 7: 06 Impact / Benefit (Impak, Kos & Kelestarian TVET)
### **Isi Utama (Bullet Points):**
* ⏱️ **Penjimatan Masa 80%:** Mempercepatkan proses koordinasi tempahan makmal daripada beberapa hari kepada beberapa saat.
* 💰 **Keberkesanan Kos:** Mengoptimumkan penggunaan makmal sedia ada tanpa memerlukan kos infrastruktur tambahan.
* 🎓 **Nilai Tambah TVET:** Meningkatkan kadar kelulusan pelajar mengulang subjek dengan penyelesaian jadual bertindih secara sistematik.
* 🌿 **Kelestarian:** Mengurangkan penggunaan borang kertas tempahan secara 100% (Paperless Campus).

---

## Slide 8: Penutup & Sesi Q&A
### **Sistem Pengurusan Makmal Pintar (Fyp1)**
* 🔗 **Kod Sumber & Repository:** GitHub (`github.com/ZenoSteel/Fyp1-LabSystem`)
* 📸 **Ujian Langsung / Live Demo Ready**
* ❓ **Sesi Soal Jawab (Q&A Panel Industri)**
