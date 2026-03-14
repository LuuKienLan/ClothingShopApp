plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.clothingshopapp"
    compileSdk = 36 // (Tôi để 34 cho ổn định, nhưng 36 cũng được)

    defaultConfig {
        applicationId = "com.example.clothingshopapp"
        minSdk = 24
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

    // Giao diện
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.browser:browser:1.8.0") // Cho VNPAY (Custom Tabs)
    implementation(libs.glide)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // osmdroid (Bản đồ)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    implementation("androidx.preference:preference:1.2.1") // Cần cho osmdroid

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-process:2.8.3")

    // Firebase (Dùng Bom để quản lý phiên bản)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Các thư viện Firebase không cần ghi version nữa
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Firebase UI (Cho Adapter)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("com.google.android.gms:play-services-location:21.2.0")
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}