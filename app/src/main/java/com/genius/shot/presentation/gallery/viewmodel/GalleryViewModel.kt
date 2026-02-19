package com.genius.shot.presentation.gallery.viewmodel

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.genius.shot.domain.model.GalleryItem
import com.genius.shot.domain.model.ImageItem
import com.genius.shot.domain.usecase.DeleteImageUseCase
import com.genius.shot.domain.usecase.ImageLoadUseCase
import com.genius.shot.domain.usecase.NotifyDataChangeUseCase
import com.genius.shot.domain.usecase.ObserveGalleryDataUseCase
import com.genius.shot.domain.usecase.SearchImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val searchImageUseCase: SearchImageUseCase
) : ViewModel() {

    // 1. 일반 갤러리 데이터 (Paging)
    val galleryData = imageLoadUseCase()
        .map { pagingData ->
            pagingData.map { GalleryItem.Image(it) }
                .insertSeparators { before: GalleryItem.Image?, after: GalleryItem.Image? ->
                    if (after == null) return@insertSeparators null

                    val beforeTime = before?.item?.dateTaken ?: return@insertSeparators GalleryItem.DateHeader(
                        formatDate(after.item.dateTaken)
                    )

                    val afterTime = after.item.dateTaken
                    if (formatDate(beforeTime) != formatDate(afterTime)) {
                        GalleryItem.DateHeader(formatDate(afterTime))
                    } else {
                        null
                    }
                }
        }
        .cachedIn(viewModelScope)

    // 2. 검색 결과 (DB에서 List로 가져옴)
    private val _searchResults = MutableStateFlow<List<GalleryItem.Image>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // 3. 선택 모드 상태
    private val _selectedItems = MutableStateFlow<Set<ImageItem>>(emptySet())
    val selectedItems = _selectedItems.asStateFlow()

    // 4. 이벤트 (권한 요청, 새로고침 등)
    private val _permissionNeededEvent = Channel<IntentSender>()
    val permissionNeededEvent = _permissionNeededEvent.receiveAsFlow()

    private val _refreshEvent = Channel<Unit>()
    val refreshEvent = _refreshEvent.receiveAsFlow()

    init {
        // 데이터 변경 감지 (삭제 등 발생 시)
        viewModelScope.launch {
            observeGalleryDataUseCase().collect {
                _refreshEvent.send(Unit)
                // 검색 중이었다면 검색 결과도 갱신
                if (_searchQuery.value.isNotEmpty()) {
                    onSearch()
                }
            }
        }
    }

    // --- 검색 로직 ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    fun onSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                // DB 검색 호출 (UseCase)
                val results = searchImageUseCase(query)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    // --- 선택 & 삭제 로직 ---
    fun toggleSelection(item: ImageItem) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (currentSelection.contains(item)) {
            currentSelection.remove(item)
        } else {
            currentSelection.add(item)
        }
        _selectedItems.value = currentSelection
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }

    fun deleteSelectedItems() {
        viewModelScope.launch {
            val itemsToDelete = _selectedItems.value.toList()
            if (itemsToDelete.isEmpty()) return@launch

            try {
                val uris = itemsToDelete.map { it.uri }
                val intentSender = deleteImageUseCase(uris)
                _permissionNeededEvent.send(intentSender)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onDeletePermissionGranted() {
        viewModelScope.launch {
            notifyDataChangeUseCase()
            clearSelection()
        }
    }

    private fun formatDate(dateMillis: Long): String {
        val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
        return formatter.format(Date(dateMillis))
    }
}