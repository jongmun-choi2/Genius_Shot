package com.genius.shot.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.genius.shot.data.source.ImagePagingSource
import com.genius.shot.domain.model.ImageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

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

    fun deleteImage(uri : Uri) {
        try {
            val resolver = context.contentResolver
            resolver.delete(uri, null, null)
        }catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

}