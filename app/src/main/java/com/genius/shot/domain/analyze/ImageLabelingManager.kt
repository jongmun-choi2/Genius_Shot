package com.genius.shot.domain.analyze

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLabelingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 정확도 70% 이상인 태그만 가져오기
    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()

    private val labeler = ImageLabeling.getClient(options)

    /**
     * 이미지에서 키워드(라벨) 리스트 추출
     */
    suspend fun getLabels(uri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val labels = labeler.process(image).await()
            Log.i("GeniusShot","tag = ${labels.map { it.text }}")
            labels.map { it.text }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}