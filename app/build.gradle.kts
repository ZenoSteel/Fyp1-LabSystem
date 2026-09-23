plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.fyp1"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fyp1.v2"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Cloud Database & Authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-database")
    implementation(libs.firebase.auth)

    // ML Kit On-Device Text Recognition (OCR for PNG/JPEG/PDF Timetable Extraction)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
