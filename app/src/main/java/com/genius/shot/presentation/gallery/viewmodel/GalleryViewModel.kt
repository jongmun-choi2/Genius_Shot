package com.genius.shot.presentation.gallery.viewmodel

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

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        if(before == null) {
            val dateStr = dateFormat.format(Date(after.item.dateTaken))
            return GalleryItem.DateHeader(dateStr)
        }

        val beforeDate = dateFormat.format(Date(before.item.dateTaken))
        val afterDate = dateFormat.format(Date(after.item.dateTaken))

        return if(beforeDate != afterDate) {
            GalleryItem.DateHeader(afterDate)
        } else {
            null
        }
    }
}