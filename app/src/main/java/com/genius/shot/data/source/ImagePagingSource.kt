package com.genius.shot.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.genius.shot.domain.model.ImageItem

class ImagePagingSource(
    private val contentResolver: ContentResolver
): PagingSource<Int, ImageItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ImageItem> {
        val position = params.key ?: 0
        val loadSize = params.loadSize // 300장

        return try {
            val images = mutableListOf<ImageItem>()

            // 1. 가져올 컬럼 정의
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED
            )

            // 2. 정렬 및 페이징 (최신순 + LIMIT/OFFSET)
            // Android 10(Q) 이상에서는 Bundle을 권장하지만, 호환성을 위해 SQL 스타일을 사용합니다.

            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT $loadSize OFFSET $position"

            // ✨ 핵심 수정: 안드로이드 버전에 따라 쿼리 방식 분기
            val cursor: Cursor? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // [방법 1] Android 8.0 (Oreo) 이상: Bundle을 사용한 정석적인 방법
                val queryArgs = Bundle().apply {
                    // SQL: LIMIT loadSize OFFSET position
                    putInt(ContentResolver.QUERY_ARG_LIMIT, loadSize)
                    putInt(ContentResolver.QUERY_ARG_OFFSET, position)

                    // 정렬 조건
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                        "${MediaStore.Images.Media.DATE_TAKEN} DESC"
                    )
                }

                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    queryArgs, // Selection 대신 Bundle 전달
                    null
                )
            } else {
                // [방법 2] Android 7.0 이하: 기존 방식 (LIMIT 꼼수 사용 가능)
                val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT $loadSize OFFSET $position"
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )
            }

            // 3. 커서 순회하며 데이터 매핑
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAdded = it.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "Unknown"
                    // DATE_TAKEN이 없으면 DATE_ADDED(초 단위) * 1000 사용
                    var date = if (dateColumn != -1) it.getLong(dateColumn) else 0L
                    if (date == 0L && dateAdded != -1) {
                        date = it.getLong(dateAdded) * 1000
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

            // 4. 다음 페이지 키 계산
            val nextKey = if (images.size < loadSize) null else position + loadSize


            LoadResult.Page(
                data = images,
                prevKey = if (position == 0) null else position - loadSize,
                nextKey = nextKey
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ImageItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}
