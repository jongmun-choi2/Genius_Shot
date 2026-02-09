package com.genius.shot.presentation.camera.viewmodel

import android.graphics.PointF
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genius.shot.domain.model.CameraUiState
import com.genius.shot.data.repository.CameraManager
import com.genius.shot.domain.analyze.ImageQualityAnalyzer
import com.genius.shot.domain.usecase.DeleteImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val qualityAnalyzer: ImageQualityAnalyzer,
    private val deleteImageUseCase: DeleteImageUseCase,

    private val cameraManager: CameraManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    // UI 상태: 팝업 표시 여부
    private val _showBlurWarning = MutableStateFlow<Uri?>(null) // 흔들린 사진 URI 저장
    val showBlurWarning = _showBlurWarning.asStateFlow()

    // 촬영된 임시 파일 URI
    private var tempPhotoUri: Uri? = null

    private var focusJob: Job? = null // 포커스 링 숨김 타이머 관리용
    init {
        // Manager의 줌 상태를 UI 상태와 동기화
        cameraManager.zoomRatio
            .onEach { zoom -> _uiState.update { it.copy(currentZoom = zoom) } }
            .launchIn(viewModelScope)
    }

    // ✨ [추가] 카메라 전환 로직
    fun toggleCamera() {
        val currentLens = _uiState.value.lensFacing
        val newLens = if (currentLens == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        // 상태 업데이트 -> UI가 감지하고 재연결 시도함
        _uiState.update { it.copy(lensFacing = newLens) }
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
                tempPhotoUri = uri

                if(qualityAnalyzer.isBlurry(uri)) {
                    // 3. 성공 시: 썸네일 업데이트 및 촬영 상태 해제
                    _showBlurWarning.value = uri
                }else {
                    _uiState.update { it.copy(lastThumbnail = uri, isCapturing = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 실패 시: 상태만 복구
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }

    // [팝업] "다시 찍기" 선택 시
    fun onRetake() {
        // 흔들린 임시 파일 삭제
        tempPhotoUri?.let { uri ->
            // 파일 삭제 로직 (ContentResolver.delete 등) 호출
            deleteImageUseCase.invoke(tempPhotoUri!!)
        }
        _showBlurWarning.value = null // 팝업 닫기
        tempPhotoUri = null
        _uiState.update { it.copy(isCapturing = false) }
    }

    // [팝업] "그래도 저장" 선택 시
    fun onKeepAnyway() {
        tempPhotoUri?.let { uri ->
            _uiState.update { it.copy(lastThumbnail = uri, isCapturing = false) }
            savePhotoToGallery(uri)
        }
        _showBlurWarning.value = null // 팝업 닫기
        tempPhotoUri = null

    }

    private fun savePhotoToGallery(uri: Uri) {
        // 갤러리 DB에 insert 하거나, 최종 저장 경로로 이동하는 로직
    }

    // CameraScreen에서 사용하기 위해 노출
    fun getManager() = cameraManager
}