package com.genius.shot.repository

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var camera: Camera? = null
    private val mainExecutor = ContextCompat.getMainExecutor(context)

    // UI에서 관찰할 현재 줌 비율
    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio = _zoomRatio.asStateFlow()

    suspend fun getCameraProvider(): ProcessCameraProvider {
        return ProcessCameraProvider.getInstance(context).await()
    }

    fun bindUseCases(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageCapture: ImageCapture
    ) {
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )

            // 줌 상태 실시간 동기화
            camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
                _zoomRatio.value = state.zoomRatio
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setZoom(ratio: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(ratio)?.await()
        }catch (e: Exception) {

        }
    }

    suspend fun startFocus(action: FocusMeteringAction) {
        try {
            camera?.cameraControl?.startFocusAndMetering(action)?.await()
        }catch (e : Exception) {

        }
    }
}