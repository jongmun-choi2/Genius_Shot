package com.genius.shot.presentation.camera.screen

// package com.example.smartcamera.presentation.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
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
    val showBlurWarningUri by viewModel.showBlurWarning.collectAsStateWithLifecycle()
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

    // ✨ 흔들림 경고 팝업
    if (showBlurWarningUri != null) {
        AlertDialog(
            onDismissRequest = { /* 바깥 터치 막기 (선택 사항) */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFCC00) // 노란색 경고
                )
            },
            title = {
                Text(text = "사진이 흔들렸어요! 😵‍💫")
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("방금 찍은 사진이 흐릿하게 나왔습니다.\n다시 찍으시겠습니까?")

                    Spacer(modifier = Modifier.height(16.dp))

                    // (선택) 흔들린 사진을 썸네일로 보여주면 더 좋음
                    @OptIn(ExperimentalGlideComposeApi::class)
                    GlideImage(
                        model = showBlurWarningUri,
                        contentDescription = "Blurry Photo",
                        contentScale = ContentScale.Crop, // 사진 꽉 차게
                        modifier = Modifier
                            .size(150.dp) // 크기 약간 키움 (잘 보이게)
                            .clip(RoundedCornerShape(12.dp))
                            .border(3.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)) // 빨간 테두리 강조
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onRetake() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("다시 찍기")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onKeepAnyway() }) {
                    Text("그냥 저장할래요", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

}
