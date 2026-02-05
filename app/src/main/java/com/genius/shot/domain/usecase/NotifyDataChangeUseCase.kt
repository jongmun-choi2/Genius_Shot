package com.genius.shot.domain.usecase

import com.genius.shot.data.repository.GalleryRepository
import javax.inject.Inject

class NotifyDataChangeUseCase @Inject constructor(
    private val repository: GalleryRepository
) {

    suspend operator fun invoke() {
        repository.notifyDataChanged()
    }

}