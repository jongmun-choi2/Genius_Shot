plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.genius.shot"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.genius.shot"
        minSdk = 32
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeBom.get()
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.material)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.graphics)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // 코루틴과 ListenableFuture를 연결해주는 핵심 라이브러리
    implementation(libs.androidx.concurrent.futures.ktx)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Glide
    implementation(libs.glide)
    implementation(libs.glide.compose)
    ksp(libs.glide.compiler)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // ML Kit
    implementation(libs.mlkit.face.detection)
    implementation(libs.mlkit.pose.detection)
    implementation(libs.mlkit.subject.segmentation)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)
}
