// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Project-level build.gradle.kts
plugins {
    // 1. App 모듈을 위한 플러그인 (필수)
    alias(libs.plugins.androidApplication) apply false

    // 2. 만약 Library 모듈이 없다면 아래 라인은 삭제하세요!
    // alias(libs.plugins.androidLibrary) apply false

    // 3. Kotlin 및 Hilt 플러그인
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    // ✨ [추가] Firebase 관련 플러그인 등록
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
