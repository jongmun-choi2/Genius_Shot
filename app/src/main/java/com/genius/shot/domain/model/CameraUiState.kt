package com.genius.shot.domain.model

import android.graphics.PointF

data class CameraUiState(
    val currentZoom: Float = 1f,
    // 포커스 UI 관련 상태 추가
    val focusPoint: PointF? = null, // 화면상 터치 좌표 (x, y)
    val isShowFocusRing: Boolean = false // 링을 보여줄지 여부
)