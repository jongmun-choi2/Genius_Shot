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
    // 기본 옵션 설정
    private val options = ImageLabelerOptions.DEFAULT_OPTIONS // 또는 Builder로 생성

    // NPE 방지를 위해 직접 getClient를 호출하는 함수로 관리
    private fun getLabeler() = ImageLabeling.getClient(options)

    suspend fun getLabels(uri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, uri)

            // 💡 getClient 호출 시점에 의존성 문제가 있으면 여기서 catch 됩니다.
            val labels = getLabeler().process(image).await()

            Log.i("GeniusShot", "Tags found: ${labels.map { it.text }}")
            labels.map { it.text }
        } catch (e: Exception) {
            Log.e("GeniusShot", "Labeling NPE or Error for $uri", e)
            emptyList()
        }
    }
}