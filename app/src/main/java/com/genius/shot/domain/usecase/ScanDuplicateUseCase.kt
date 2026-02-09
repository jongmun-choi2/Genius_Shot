package com.genius.shot.domain.usecase

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.analyze.DuplicateAnalyzer
import com.genius.shot.domain.model.AnalysisResult
import com.genius.shot.domain.model.ImageAnalysisResult
import com.genius.shot.domain.model.ScanStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import javax.inject.Inject

class ScanDuplicateUseCase @Inject constructor(
    private val imageRepository: GalleryRepository,
    private val analyzer: DuplicateAnalyzer,
    @ApplicationContext private val context: Context
) {

    operator fun invoke(offset: Int, limit: Int): Flow<ScanStatus> = flow {
        // 1. Repository에서 지정된 범위만큼 가져옴
        val images = imageRepository.getImagesChunk(limit, offset)

        if (images.isEmpty()) {
            emit(ScanStatus.Complete(emptyList(), true))
            return@flow
        }

        val analysisResults = mutableListOf<ImageAnalysisResult>()

        val UPDATE_INTERVAL = 20

        // 2. 가져온 이미지만 분석 (리사이징 적용)
        images.forEachIndexed { index, item ->
            val bitmap = loadBitmap(item.uri)
            if (bitmap != null) {
                val result = analyzer.analyzeImage(item.uri, bitmap, item.dateTaken)
                analysisResults.add(result)
                bitmap.recycle() // 메모리 해제

            }

            yield()

            if((index + 1) % UPDATE_INTERVAL == 0) {
                emit(ScanStatus.Progress("${index + 1}장 분석 중입니다."))
            }
        }

        // 이번에 가져온 개수가 요청한 limit보다 적으면 마지막 페이지임
        val isLastBatch = images.size < limit
        emit(ScanStatus.Complete(analysisResults, isLastBatch))

    }.flowOn(Dispatchers.Default)

    // (이전 답변의 OOM 방지용 리사이징 loadBitmap 함수 사용)
    private fun loadBitmap(uri: Uri): android.graphics.Bitmap? {
        // ... calculateInSampleSize 적용된 코드 ...
        return try {
            val contentResolver = context.contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            // 640px 수준으로 다운샘플링
            options.inSampleSize = calculateInSampleSize(options, 640, 640)
            options.inJustDecodeBounds = false

            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        } catch (e: Exception) { null }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // ... (이전 답변과 동일한 로직) ...
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}