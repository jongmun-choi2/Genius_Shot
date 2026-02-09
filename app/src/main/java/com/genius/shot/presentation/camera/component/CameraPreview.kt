package com.genius.shot.presentation.camera.component

import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genius.shot.presentation.camera.viewmodel.CameraViewModel

@Composable
fun CameraPreview(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 1. 카메라 바인딩
    LaunchedEffect(uiState.lensFacing) {
        val manager = viewModel.getManager()
        val provider = manager.getCameraProvider()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.Builder().requireLensFacing(uiState.lensFacing).build()

        manager.bindUseCases(lifecycleOwner, provider, preview, imageCapture, cameraSelector)
    }



    // 2. 제스처 레이어
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                // 줌 제스처 (멀티터치)
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) viewModel.onZoom(zoom)
                }
            }
            .pointerInput(Unit) {
                // 포커스 제스처 (탭)
                detectTapGestures { offset ->
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    viewModel.onFocus(action, offset.x, offset.y)
                }
            }
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (uiState.isShowFocusRing && uiState.focusPoint != null) {
            FocusRing(center = uiState.focusPoint!!)
        }
    }
}