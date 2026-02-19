plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
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
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
        buildTypes {
            debug {
                // ✨ 2. 개발 버전 전용 설정
                // 패키지명 뒤에 .dev가 붙어서 운영 앱과 동시에 설치 가능해짐

                // 버전 이름 뒤에 -DEV가 붙음 (예: 1.0-DEV)
                versionNameSuffix = "-DEV"

                // 코드에서 쓸 수 있는 상수 (IS_DEV = true)
                buildConfigField("boolean", "IS_DEV", "true")

                // (선택) 앱 이름도 다르게 표시하려면 resValue 사용
                // resValue("string", "app_name", "Genius Shot (Dev)")
            }

            release {
                // ✨ 3. 운영 버전 설정
                isDebuggable = false
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )

                // 코드에서 쓸 수 있는 상수 (IS_DEV = false)
                buildConfigField("boolean", "IS_DEV", "false")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.play.services.ads.api)
    implementation(libs.image.labeling.default.common)
    implementation(libs.translate)
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
    implementation(libs.mlkit.obj.detection)
    // 구글의 선행 학습된 모델을 이용해 이미지를 벡터로 변환합니다.
    implementation(libs.tensorflow.lite.task.vision)

    implementation(libs.kotlinx.coroutines.play.services)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // ✨ [추가] Firebase 의존성
    // BOM을 사용하면 개별 라이브러리 버전을 적지 않아도 자동으로 맞춰줍니다.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics) // 크래시 분석에 사용자 통계가 도움됨

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager + Hilt Support
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
