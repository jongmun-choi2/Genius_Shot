package com.genius.shot.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.genius.shot.data.source.ImagePagingSource
import com.genius.shot.domain.model.ImageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
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

    suspend fun getImagesChunk(limit: Int, offset: Int): List<ImageItem> {
        return withContext(Dispatchers.IO) {
            val images = mutableListOf<ImageItem>()

            // ... (아까 작성했던 Bundle 또는 SQL 쿼리 로직을 여기에 넣습니다) ...
            // Android 버전 분기 처리 및 query() 실행 코드

            // (예시 코드)
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            val selection = null // 전체 조회

            val args = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            }
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED),
                args,
                null
            )?.use { cursor ->
                // ... cursor -> ImageItem 변환 로직
                // images.add(...)
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAdded = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown"
                    // DATE_TAKEN이 없으면 DATE_ADDED(초 단위) * 1000 사용
                    var date = if (dateColumn != -1) cursor.getLong(dateColumn) else 0L
                    if (date == 0L && dateAdded != -1) {
                        date = cursor.getLong(dateAdded) * 1000
                    }
                    // 그래도 0이면 현재 시간으로 대체 (정렬 순서 꼬임 방지)
                    if (date == 0L) date = System.currentTimeMillis()

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    images.add(ImageItem(id, contentUri, name, date))
                }
            }

            images // 순수 리스트 반환
        }
    }

    fun getGalleryImages(): Flow<PagingData<ImageItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                initialLoadSize = 50
            ),
            pagingSourceFactory = { ImagePagingSource(this) }
        ).flow
    }

    fun getDeleteRequest(uris: List<Uri>): IntentSender {
        // 팝업 한 번으로 여러 장 삭제 권한 요청 생성
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }

}