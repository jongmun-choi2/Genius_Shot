package com.genius.shot.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.genius.shot.data.db.ImageLabelDao
import com.genius.shot.data.db.ImageLabelEntity
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.analyze.ImageLabelingManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LabelingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: GalleryRepository,
    private val labelingManager: ImageLabelingManager,
    private val labelDao: ImageLabelDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. 갤러리 전체 사진 가져오기 (가볍게 ID, URI만)
            val allImages = repository.getAllImageIdsAndUris()

            // 2. 이미 DB에 분석된 ID 가져오기
            val analyzedIds = labelDao.getAnalyzedImageIds().toSet()

            // 3. 분석 안 된 '새로운' 사진만 골라내기
            val newImages = allImages.filter { it.id !in analyzedIds }

            if (newImages.isEmpty()) return Result.success()

            // 4. 순차적으로 분석 시작
            newImages.forEach { image ->
                if (isStopped) return Result.success() // 작업 중지 요청 시 안전하게 종료

                try {
                    val labels = labelingManager.getLabels(image.uri)
                    Log.i("LabelingWorker", "Image: $image, Labels: $labels")
                    // 분석 결과 DB 저장
                    labelDao.insert(
                        ImageLabelEntity(
                            id = image.id,
                            uri = image.uri.toString(),
                            dateTaken = image.dateTaken,
                            labels = labels.joinToString(",") // 예: "Cat,Pet"
                        )
                    )
                } catch (e: Exception) {
                    // 한 장 실패해도 다음 장으로 넘어감
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}