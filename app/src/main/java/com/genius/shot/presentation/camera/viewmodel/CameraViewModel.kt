package com.genius.shot.presentation.camera.viewmodel

// package com.example.smartcamera.presentation.camera

import android.graphics.PointF
import androidx.camera.core.FocusMeteringAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genius.shot.domain.model.CameraUiState
import com.genius.shot.data.repository.CameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()
    private var focusJob: Job? = null // 포커스 링 숨김 타이머 관리용
    init {
        // Manager의 줌 상태를 UI 상태와 동기화
        cameraManager.zoomRatio
            .onEach { zoom -> _uiState.update { it.copy(currentZoom = zoom) } }
            .launchIn(viewModelScope)
    }

    fun onZoom(scale: Float) {
        viewModelScope.launch {
            val newZoom = (uiState.value.currentZoom * scale).coerceIn(1f, 10f)
            cameraManager.setZoom(newZoom)
        }
    }


    // 인자 수정: Action뿐만 아니라 화면 좌표(x, y)도 받음
    fun onFocus(action: FocusMeteringAction, x: Float, y: Float) {
        // 1. 기존 타이머 취소 (연속 터치 대응)
        focusJob?.cancel()

        viewModelScope.launch {
            // 2. UI에 링 표시 및 좌표 업데이트
            _uiState.update {
                it.copy(
                    focusPoint = PointF(x, y),
                    isShowFocusRing = true
                )
            }

            // 3. 실제 카메라 하드웨어에 포커스 요청
            try {
                cameraManager.startFocus(action)
            } catch (e: Exception) {
                // 포커스 실패해도 UI는 반응했으니 패스
            }

        }
    }

    fun capturePhoto() {
        // 이미 촬영 중이면 무시 (중복 클릭 방지)
        if (uiState.value.isCapturing) return

        viewModelScope.launch {
            // 1. UI 상태 변경: 촬영 중 (로딩 표시 등) & 셔터 효과 트리거
            _uiState.update { it.copy(isCapturing = true) }

            try {
                // 2. 촬영 수행 (비동기 대기)
                val uri = cameraManager.takePhoto()

                // 3. 성공 시: 썸네일 업데이트 및 촬영 상태 해제
                _uiState.update { it.copy(lastThumbnail = uri, isCapturing = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                // 실패 시: 상태만 복구
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    // CameraScreen에서 사용하기 위해 노출
    fun getManager() = cameraManager
}