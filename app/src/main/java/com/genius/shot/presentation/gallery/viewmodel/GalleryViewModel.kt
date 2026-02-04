package com.genius.shot.presentation.gallery.viewmodel

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.model.GalleryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel(){

    val galleryData: Flow<PagingData<GalleryItem>> = galleryRepository.getGalleryImages()
        .map { pagingData ->
            pagingData.map { GalleryItem.Image(it) }
        }
        .map { pagingData ->
            pagingData.insertSeparators { before, after ->
                shouldAddSeparator(before, after)
            }
        }
        .cachedIn(viewModelScope)

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