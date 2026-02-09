package com.genius.shot.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.model.ImageItem

class ImagePagingSource(
    private val repository: GalleryRepository
): PagingSource<Int, ImageItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ImageItem> {
        // key가 없으면 0부터 시작 (Offset)
        val position = params.key ?: 0
        val loadSize = params.loadSize

        return try {
            /* * [기존 코드 삭제됨]
             * val projection = ...
             * val cursor = contentResolver.query(...)
             * ... cursor looping ...
             */

            // ✨ [수정] Repository의 공통 함수 호출! (코드가 매우 간결해짐)
            // UI Paging에서는 loadSize가 보통 30~60 정도로 들어옵니다.
            val images = repository.getImagesChunk(limit = loadSize, offset = position)

            // 다음 페이지 키 계산
            // 가져온 데이터가 요청한 사이즈보다 적으면 더 이상 데이터가 없는 것임
            val nextKey = if (images.size < loadSize) null else position + loadSize

            val prevKey = if (position == 0) null else position - loadSize
            Log.i("GeniusShot", "imageCount = ${nextKey}")
            LoadResult.Page(
                data = images,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (e: Exception) {
            e.printStackTrace()
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
