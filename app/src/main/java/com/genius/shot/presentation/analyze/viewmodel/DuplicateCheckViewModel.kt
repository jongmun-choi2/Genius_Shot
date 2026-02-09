package com.genius.shot.presentation.analyze.viewmodel

import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genius.shot.domain.analyze.DuplicateAnalyzer
import com.genius.shot.domain.model.ImageAnalysisResult
import com.genius.shot.domain.model.ScanStatus
import com.genius.shot.domain.usecase.DeleteImageUseCase
import com.genius.shot.domain.usecase.NotifyDataChangeUseCase
import com.genius.shot.domain.usecase.ScanDuplicateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuplicateCheckViewModel @Inject constructor(
    private val scanDuplicateUseCase: ScanDuplicateUseCase,
    private val analyzer: DuplicateAnalyzer,
    private val deleteDuplicateUseCase: DeleteImageUseCase,
    private val notifyDataChangeUseCase: NotifyDataChangeUseCase
) : ViewModel() {

    private val BATCH_SIZE = 300

    // UI 상태
    private val _duplicateGroups = MutableStateFlow<List<List<Uri>>>(emptyList())
    val duplicateGroups = _duplicateGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 진행 상황 (현재까지 분석한 수)
    private val _analyzedCount = MutableStateFlow(0)
    val analyzedCount = _analyzedCount.asStateFlow()

    private val _progressMessage = MutableStateFlow("0")
    val progressMessage = _progressMessage.asStateFlow()

    // 더 분석할 이미지가 남았는지?
    private val _hasMoreImages = MutableStateFlow(true)
    val hasMoreImages = _hasMoreImages.asStateFlow()

    private val _permissionNeededEvent = Channel<IntentSender>()
    val permissionNeededEvent = _permissionNeededEvent.receiveAsFlow()

    // ✨ 내부 메모리에 분석 결과(벡터) 누적
    private val accumulatedResults = mutableListOf<ImageAnalysisResult>()
    private var currentOffset = 0

    // 삭제 후 처리를 위해 '방금 삭제 요청한 그룹'을 임시 저장
    private var lastDeletedGroup: List<Uri>? = null

    // 초기 진입 시 자동 실행
    init {
        scanNextBatch()
    }

    //  다음 300장 분석 버튼 클릭 시 호출
    fun scanNextBatch() {
        if (_isLoading.value) return // 이미 분석 중이면 무시

        viewModelScope.launch {
            _isLoading.value = true
            _progressMessage.value = "분석중..."
            try {
                // 1. UseCase 호출 (현재 offset부터 300장)
                scanDuplicateUseCase(currentOffset, BATCH_SIZE).collect() { result ->
                    when (result) {
                        is ScanStatus.Progress -> {
                            _progressMessage.value = result.message
                        }

                        is ScanStatus.Complete -> {
                            accumulatedResults.addAll(result.duplicateGroups)
                            currentOffset += result.duplicateGroups.size

                            _analyzedCount.value = currentOffset
                            _hasMoreImages.value = !result.isLastPage

                            if (accumulatedResults.isNotEmpty()) {
                                val groups = analyzer.findDuplicateGroups(accumulatedResults)
                                _duplicateGroups.value = groups
                            }
                        }

                        is ScanStatus.Error -> {
                        }
                    }

                }


            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 처리 (Toast 등)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // UI에서 "이 그룹 정리해줘"라고 요청했을 때 실행
    fun deleteDuplicatesInGroup(keepUri: Uri, groupUris: List<Uri>) {
        viewModelScope.launch {
            // 1. 남길 사진을 제외한 나머지 리스트 추출
            val urisToDelete = groupUris.filter { it != keepUri }

            if (urisToDelete.isEmpty()) return@launch

            try {
                // 2. 삭제 권한 요청 (Android 11+ 대응)
                val intentSender = deleteDuplicateUseCase(urisToDelete)
                _permissionNeededEvent.send(intentSender)

                // 주의: 실제 리스트 갱신은 삭제가 성공한 후(onActivityResult)에 해야 하지만,
                // UX 편의상 권한 요청을 보내면서 해당 그룹을 메모리에서 미리 지우거나,
                // 화면을 갱신하는 로직을 추가할 수 있습니다.
                // 여기서는 간단히 삭제 요청을 보내는 것까지만 구현합니다.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 삭제 성공 시 호출 (화면 갱신용)
    fun onDeletionSuccess() {
        viewModelScope.launch {
            notifyDataChangeUseCase() // 갤러리 갱신
            // 현재 중복 목록에서도 제거해야 하는데,
            // 가장 쉬운 방법은 재스캔이지만 무거우므로
            // 실제 앱에서는 메모리상에서 해당 그룹을 remove하는 로직을 추가해야 합니다.
            lastDeletedGroup?.let { deletedGroup ->
                // 현재 보여지고 있는 목록에서 제거
                _duplicateGroups.value = _duplicateGroups.value.filter { it != deletedGroup }

                // (선택) 누적된 분석 결과(accumulatedResults)에서도 제거해야
                // '다음 300장' 눌렀을 때 다시 안 튀어나옵니다.
                // 하지만 URI 비교 비용이 들므로, 여기선 UI 목록 갱신만 우선 처리합니다.

                lastDeletedGroup = null
            }
        }
    }

}