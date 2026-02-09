package com.genius.shot.domain.usecase

import android.content.IntentSender
import android.net.Uri
import android.os.Build
import com.genius.shot.data.repository.GalleryRepository
import javax.inject.Inject

class DeleteImageUseCase @Inject constructor(
    private val repository: GalleryRepository
) {

    // Android 11+ (minSDK 32) 방식
    operator fun invoke(uri: Uri): IntentSender {
        return repository.getDeleteRequest(listOf(uri))
    }

    // 다중 삭제용 오버로딩
    operator fun invoke(uris: List<Uri>): IntentSender {
        return repository.getDeleteRequest(uris)
    }

}