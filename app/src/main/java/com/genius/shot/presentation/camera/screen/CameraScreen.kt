package com.genius.shot.presentation.camera.screen

// package com.example.smartcamera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genius.shot.presentation.camera.component.CameraControls
import com.genius.shot.presentation.camera.component.CameraPreview
import com.genius.shot.presentation.camera.viewmodel.CameraViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(
    onGalleryClick: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 셔터 효과 상태 관리
    var showShutterEffect by remember { mutableStateOf(false) }

    // isCapturing이 true가 되면 셔터 효과를 잠깐 보여줌
    LaunchedEffect(uiState.isCapturing) {
        if (uiState.isCapturing) {
            showShutterEffect = true
            delay(100) // 0.1초 동안 검은 화면
            showShutterEffect = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 프리뷰
        CameraPreview(viewModel = viewModel, modifier = Modifier.fillMaxSize())

        // 2. 셔터 플래시 효과 (검은 화면 깜빡임)
        if (showShutterEffect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // 3. 컨트롤 UI
        CameraControls(
            lastThumbnail = uiState.lastThumbnail,
            isCapturing = uiState.isCapturing,
            onCaptureClick = { viewModel.capturePhoto() }, // 함수 연결
            onGalleryClick = onGalleryClick,
            currentZoomRatio = uiState.currentZoom
        )
    }
}