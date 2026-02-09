package com.genius.shot.presentation.gallery.viewmodel

import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.model.GalleryItem
import com.genius.shot.domain.model.ImageItem
import com.genius.shot.domain.usecase.DeleteImageUseCase
import com.genius.shot.domain.usecase.ImageLoadUseCase
import com.genius.shot.domain.usecase.NotifyDataChangeUseCase
import com.genius.shot.domain.usecase.ObserveGalleryDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val imageLoadUseCase: ImageLoadUseCase,
    private val observeGalleryDataUseCase: ObserveGalleryDataUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val notifyDataChangeUseCase: NotifyDataChangeUseCase,
) : ViewModel(){

    val galleryData: Flow<PagingData<GalleryItem>> = imageLoadUseCase()
        .map { pagingData ->
            pagingData.map { GalleryItem.Image(it) }
        }
        .map { pagingData ->
            pagingData.insertSeparators { before, after ->
                shouldAddSeparator(before, after)
            }
        }
        .cachedIn(viewModelScope)

    private val _selectedItems = MutableStateFlow<Set<ImageItem>>(setOf())
    val selectedItems: StateFlow<Set<ImageItem>> = _selectedItems.asStateFlow()

    // ✨ UI에 "팝업 띄워줘"라고 요청할 채널
    private val _permissionNeededEvent = Channel<IntentSender>()
    val permissionNeededEvent = _permissionNeededEvent.receiveAsFlow()

    private val _refreshEvent = Channel<Unit>()
    val refreshEvent = _refreshEvent.receiveAsFlow()

    val isSelectionMode: Boolean
        get() = _selectedItems.value.isNotEmpty()


    init {
        viewModelScope.launch {
            observeGalleryDataUseCase().collect {
                _refreshEvent.send(Unit)
            }
        }
    }

    fun toggleSelection(item: ImageItem) {
        _selectedItems.update { currentSet ->
            if (currentSet.contains(item)) {
                currentSet - item
            } else {
                currentSet + item
            }
        }
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }

    fun deleteSelectedItems() {

        viewModelScope.launch {
            val itemsToDelete = _selectedItems.value.toList()
            if (itemsToDelete.isEmpty()) return@launch

            val uris = itemsToDelete.map { it.uri }

            try {
                // minSdk 32이므로 무조건 createDeleteRequest 사용 가능!
                val intentSender = deleteImageUseCase(uris)
                _permissionNeededEvent.send(intentSender)
            } catch (e: Exception) {
                e.printStackTrace()
                // 혹시 모를 에러 처리
            }
        }
    }

    // ✨ [수정] 권한 획득 후 로직도 단순화
    // createDeleteRequest는 "허용" 누르는 순간 시스템이 삭제함 -> UI만 갱신
    fun onDeletePermissionGranted() {
        clearSelection()
        viewModelScope.launch {
            notifyDataChangeUseCase()
        }
    }

    private fun shouldAddSeparator(
        before: GalleryItem.Image?,
        after: GalleryItem.Image?
    ): GalleryItem.DateHeader? {
        if(after == null) return null

        val afterDateStr = getPrettyDateString(after.item.dateTaken)

        if (before == null) {
            return GalleryItem.DateHeader(afterDateStr)
        }

        val beforeDateStr = getPrettyDateString(before.item.dateTaken)

        return if (beforeDateStr != afterDateStr) {
            GalleryItem.DateHeader(afterDateStr)
        } else {
            null
        }
    }

    // ✨ 예쁜 날짜 문자열 생성 함수
    private fun getPrettyDateString(timestamp: Long): String {
        val now = System.currentTimeMillis()

        // Android DateUtils를 쓰면 "오늘", "어제" 처리가 아주 쉽습니다.
        return if (DateUtils.isToday(timestamp)) {
            "오늘"
        } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
            "어제"
        } else {
            // 그 외: "2월 4일 (수)" 형태
            val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
            dateFormat.format(Date(timestamp))
        }
    }
}