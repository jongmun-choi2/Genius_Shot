package com.genius.shot.domain.model

import android.graphics.PointF
import android.net.Uri
import androidx.camera.core.CameraSelector

data class CameraUiState(
    val lastThumbnail: Uri? = null,
    val isCapturing: Boolean = false,
    val currentZoom: Float = 1f,
    // 포커스 UI 관련 상태 추가
    val focusPoint: PointF? = null, // 화면상 터치 좌표 (x, y)
    val isShowFocusRing: Boolean = false, // 링을 보여줄지 여부
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK
)