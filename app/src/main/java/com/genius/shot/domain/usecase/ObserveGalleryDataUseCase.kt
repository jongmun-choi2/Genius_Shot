package com.genius.shot.domain.usecase

import com.genius.shot.data.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGalleryDataUseCase @Inject constructor(
    private val repository: GalleryRepository
) {

    operator fun invoke(): Flow<Unit> {
        return repository.dataChangedEvent
    }


}