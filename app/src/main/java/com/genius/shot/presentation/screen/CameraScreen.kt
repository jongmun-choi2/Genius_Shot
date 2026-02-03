package com.genius.shot.presentation.screen

// package com.example.smartcamera.presentation.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.genius.shot.presentation.camera.CameraPreview
import com.genius.shot.viewmodel.CameraViewModel

@Composable
fun CameraScreen(onGalleryClick: () -> Unit) {
    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val viewModel: CameraViewModel = hiltViewModel(viewModelStoreOwner = owner)
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 프리뷰
        CameraPreview(viewModel = viewModel, modifier = Modifier.fillMaxSize())

        // 줌 수치 표시 (상단 중앙)
        Text(
            text = "Zoom: ${String.format("%.1f", uiState.currentZoom)}x",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        // 여기에 촬영 버튼 등을 추가하면 됩니다.
    }
}