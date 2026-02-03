package com.genius.shot.presentation.component

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.util.Locale

@Composable
fun CameraControls(
    lastThumbnail: Uri?,
    isCapturing: Boolean,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    currentZoomRatio: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp)
    ) {
        // 1. 줌 배율 표시 (중앙 하단, 버튼 위)
        // 소수점 1자리까지만 깔끔하게 표시 (예: 1.5x)
        ZoomIndicator(
            zoomRatio = currentZoomRatio,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp) // 촬영 버튼 위에 위치
        )

        // 2. 촬영 버튼 (중앙 하단)
        CaptureButton(
            onClick = onCaptureClick,
            isEnabled = !isCapturing, // 촬영 중엔 버튼 비활성화
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 3. 갤러리 진입 버튼 (우측 하단)
        GalleryThumbnailButton(
            lastThumbnail = lastThumbnail,
            onClick = onGalleryClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 12.dp) // 위치 미세 조정
        )
    }

}
// 줌 상태 표시용 작은 배지
@Composable
fun ZoomIndicator(
    zoomRatio: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = String.format(locale = Locale.getDefault(), "%.1fx", zoomRatio),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 갤러리 썸네일 버튼 (Glide 사용)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryThumbnailButton(
    lastThumbnail: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(Color.DarkGray)
            .border(2.dp, Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (lastThumbnail != null && lastThumbnail != Uri.EMPTY) {
            GlideImage(
                model = lastThumbnail,
                contentDescription = "Gallery",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 썸네일 없을 때 기본 아이콘
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Gallery",
                tint = Color.White
            )
        }
    }
}