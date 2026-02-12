# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ----------------------------------------------------------------------------
# 1. Android & Compose 기본 규칙
# ----------------------------------------------------------------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Compose 관련 (대부분 R8이 알아서 처리하지만 안전장치)
-keep class androidx.compose.ui.** { *; }

# ----------------------------------------------------------------------------
# 2. Hilt (Dependency Injection)
# ----------------------------------------------------------------------------
-keep class com.genius.shot.GeniusShotApplication { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends android.app.Activity

# Hilt가 생성하는 클래스들 유지
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }

# ----------------------------------------------------------------------------
# 3. Glide (Image Loading)
# ----------------------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# ----------------------------------------------------------------------------
# 4. ML Kit & TensorFlow Lite (AI)
# ----------------------------------------------------------------------------
-keep class com.google.android.gms.internal.mlkit_vision_** { *; }
-keep class com.google.mlkit.** { *; }
-keep class org.tensorflow.lite.** { *; }

# ----------------------------------------------------------------------------
# 5. Google AdMob (Ads)
# ----------------------------------------------------------------------------
-keep class com.google.android.gms.ads.** { *; }

# ----------------------------------------------------------------------------
# 6. Firebase (Crashlytics)
# ----------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }

# ----------------------------------------------------------------------------
# 7. Kotlin Coroutines
# ----------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# ----------------------------------------------------------------------------
# 8. 앱 데이터 모델 (선택 사항)
# ----------------------------------------------------------------------------
# 데이터 클래스들이 Reflection(Gson 등)으로 변환된다면 유지해야 함
# 현재 프로젝트는 직접 사용하지 않는 것 같지만, 혹시 모르니 추가
-keep class com.genius.shot.domain.model.** { *; }