package com.genius.shot.data.repository

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.genius.shot.data.source.ImagePagingSource
import com.genius.shot.domain.model.ImageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _dataChangedEvent = MutableSharedFlow<Unit>()
    val dataChangedEvent = _dataChangedEvent.asSharedFlow()

    suspend fun notifyDataChanged() {
        _dataChangedEvent.emit(Unit)
    }


    fun getGalleryImages(): Flow<PagingData<ImageItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 300,
                enablePlaceholders = false,
                initialLoadSize = 300
            ),
            pagingSourceFactory = { ImagePagingSource(context.contentResolver) }
        ).flow
    }

    fun getDeleteRequest(uris: List<Uri>): IntentSender {
        // 팝업 한 번으로 여러 장 삭제 권한 요청 생성
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }

}