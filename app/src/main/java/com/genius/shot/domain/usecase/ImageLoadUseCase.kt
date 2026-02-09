package com.genius.shot.domain.usecase

import androidx.paging.PagingData
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.model.ImageItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImageLoadUseCase @Inject constructor(
    private val repository: GalleryRepository
) {

    operator fun invoke(): Flow<PagingData<ImageItem>> {
        return repository.getGalleryImages()
    }

}