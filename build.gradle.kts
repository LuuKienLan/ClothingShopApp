// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    // Chỉ định nghĩa plugin ở đây, không áp dụng
    alias(libs.plugins.google.gms.google.services) apply false
}