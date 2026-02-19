package com.genius.shot.data.repository

import android.content.IntentSender
import android.net.Uri
import androidx.paging.PagingData
import com.genius.shot.domain.model.ImageIdAndUri
import com.genius.shot.domain.model.ImageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface GalleryRepository {
    // 데이터 변경 알림 (삭제 등 발생 시)
    val dataChangedEvent: SharedFlow<Unit>
    suspend fun notifyDataChanged()

    // 페이징용: 부분적으로 가져오기
    suspend fun getImagesChunk(limit: Int, offset: Int): List<ImageItem>

    // PagingData Flow 가져오기
    fun getGalleryImages(): Flow<PagingData<ImageItem>>

    // 삭제 권한 요청 IntentSender 생성
    fun getDeleteRequest(uris: List<Uri>): IntentSender

    // ✨ [추가] 워커용: 전체 사진의 ID와 URI만 빠르게 가져오기
    suspend fun getAllImageIdsAndUris(): List<ImageIdAndUri>
}