package com.genius.shot.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.genius.shot.data.source.ImagePagingSource
import com.genius.shot.domain.model.ImageIdAndUri
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
class GalleryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GalleryRepository {

    private val _dataChangedEvent = MutableSharedFlow<Unit>()
    override val dataChangedEvent = _dataChangedEvent.asSharedFlow()

    override suspend fun notifyDataChanged() {
        _dataChangedEvent.emit(Unit)
    }

    override suspend fun getImagesChunk(limit: Int, offset: Int): List<ImageItem> {
        return withContext(Dispatchers.IO) {
            val images = mutableListOf<ImageItem>()
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            val args = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            }

            try {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.DATE_ADDED
                    ),
                    args,
                    null
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    val dateAdded = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: "Unknown"
                        var date = if (dateColumn != -1) cursor.getLong(dateColumn) else 0L
                        if (date == 0L && dateAdded != -1) {
                            date = cursor.getLong(dateAdded) * 1000
                        }
                        if (date == 0L) date = System.currentTimeMillis()

                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        images.add(ImageItem(id, contentUri, name, date))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            images
        }
    }

    override fun getGalleryImages(): Flow<PagingData<ImageItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                initialLoadSize = 50
            ),
            // Interface(this)를 넘겨주면 PagingSource가 알아서 Interface 메소드를 호출합니다.
            pagingSourceFactory = { ImagePagingSource(this) }
        ).flow
    }

    override fun getDeleteRequest(uris: List<Uri>): IntentSender {
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }

    // ✨ [추가] 워커용 전체 스캔 함수 구현
    override suspend fun getAllImageIdsAndUris(): List<ImageIdAndUri> {
        return withContext(Dispatchers.IO) {
            val imageList = mutableListOf<ImageIdAndUri>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null, null,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        imageList.add(ImageIdAndUri(id, contentUri, dateTaken))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            imageList
        }
    }
}