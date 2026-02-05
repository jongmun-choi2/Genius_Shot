package com.genius.shot.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var camera: Camera? = null

    private var imageCapture: ImageCapture? = null

    private val mainExecutor = ContextCompat.getMainExecutor(context)

    // UI에서 관찰할 현재 줌 비율
    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio = _zoomRatio.asStateFlow()

    suspend fun getCameraProvider(): ProcessCameraProvider {
        return ProcessCameraProvider.Companion.getInstance(context).await()
    }

    suspend fun takePhoto(): Uri = suspendCancellableCoroutine { continuation ->
        val imageCapture = this.imageCapture ?: run {
            continuation.resumeWithException(IllegalStateException("Camera is not bound"))
            return@suspendCancellableCoroutine
        }

        val timestamp = System.currentTimeMillis()
        // 1. 날짜 폴더명 (예: 2026-02-03)
        val dateFolder = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp)
        // 2. 파일명 (예: 20260203_143001.jpg)
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(timestamp)

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            // ✨ 핵심 변경 사항: DCIM 폴더 아래 날짜 폴더 생성
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                // 결과: DCIM/2026-02-03/파일.jpg
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/$dateFolder"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.EMPTY

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(savedUri, contentValues, null, null)
                    }

                    // 미디어 스캔으로 갤러리에 즉시 반영
                    MediaScannerConnection.scanFile(context, arrayOf(savedUri.path), null, null)

                    continuation.resume(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    continuation.resumeWithException(exc)
                }
            }
        )
    }

    fun bindUseCases(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        imageCapture: ImageCapture
    ) {

        this.imageCapture = imageCapture

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