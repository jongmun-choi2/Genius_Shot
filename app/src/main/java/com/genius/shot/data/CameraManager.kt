package com.genius.shot.data

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private var cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    fun getCameraProvider(onReady: (ProcessCameraProvider) -> Unit) {
        cameraProviderFuture.addListener({
            onReady(cameraProviderFuture.get())
        }, mainExecutor)
    }

    // 기능 1-1: 날짜별 경로 생성 유틸
    fun getSaveLocation(): String {
        val datePath = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "Pictures/SmartCamera/$datePath"
    }
}