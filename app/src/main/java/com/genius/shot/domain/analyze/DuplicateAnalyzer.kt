package com.genius.shot.domain.analyze

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import com.genius.shot.domain.model.ImageAnalysisResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.core.graphics.scale
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DuplicateAnalyzer @Inject constructor(@ApplicationContext private val context: Context) {

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
    )





    suspend fun analyzeImage(uri: Uri, bitmap: Bitmap, dateTaken: Long) : ImageAnalysisResult {
        return withContext(Dispatchers.IO) {
            val image = InputImage.fromBitmap(bitmap, 0)

            val objects = objectDetector.process(image).await()
            val hasPerson = objects.any { it.labels.any { label -> label.text == "Person" } }

            if(hasPerson) {
                val pose = poseDetector.process(image).await()
                val landmarks = pose.allPoseLandmarks

                val vector = if(landmarks.isEmpty()) {
                    FloatArray(66) { 0f }
                } else {
                    val floatList = mutableListOf<Float>()
                    for(landmark in landmarks) {
                        floatList.add(landmark.position.x)
                        floatList.add(landmark.position.y)
                    }
                    floatList.toFloatArray()
                }
                ImageAnalysisResult(uri, hasPerson, vector, dateTaken)
            } else {
                val vector = extractColorHistogram(bitmap)
                ImageAnalysisResult(uri, false, vector, dateTaken)
            }
        }
    }

    fun calculateSimilarity(result1: ImageAnalysisResult, result2: ImageAnalysisResult): Float {
        if (result1.hasPerson != result2.hasPerson) return 0f

        return if (result1.hasPerson) {
            // [사람 비교: Pose]
            val distance = weightedEuclideanDistance(result1.vector, result2.vector)

            // ✨ [핵심 수정] 기준 강화 (Sensitivity Tuning)
            // 기존: 1.0f - (distance / 1000f) -> 100px 움직여도 0.1 감점 (너무 관대함)
            // 수정: 1.0f - (distance / 200f)  -> 100px 움직이면 0.5 감점 (가차 없음)
            //
            // 거리가 200 (벡터 거리) 이상 차이나면 유사도 0점 처리.
            // 즉, 손이나 고개를 조금만 돌려도 유사도가 확 떨어져서 '다른 사진'이 됩니다.
            val similarity = (1.0f - (distance / 150f)).coerceIn(0f, 1f)

            // 디버깅용 로그 (필요시 주석 해제하여 Logcat 확인)
            // Log.d("Analyzer", "Distance: $distance, Similarity: $similarity")

            similarity
        } else {
            // [배경 비교: Color]
            // 배경은 조명 차이 등이 있을 수 있으므로 기존 코사인 유사도 유지
            cosineSimilarity(result1.vector, result2.vector)
        }

    }

    private fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
            normA += vector1[i].pow(2f)
            normB += vector2[i].pow(2f)
        }
        return (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
    }

    /**
     * ✨ [신규] 부위별 중요도를 다르게 적용한 거리 계산
     * 손목, 팔꿈치가 움직이면 거리가 급격히 늘어남
     */
    private fun weightedEuclideanDistance(v1: FloatArray, v2: FloatArray): Float {
        var sum = 0.0

        // ML Kit Pose Landmark 인덱스 (총 33개 포인트)
        // 0~10: 얼굴 (코, 눈, 귀, 입)
        // 11~12: 어깨
        // 13~14: 팔꿈치 (Elbow)
        // 15~16: 손목 (Wrist)
        // 23~24: 힙
        // 25~26: 무릎
        // 27~28: 발목
        // ...

        // 포인트 개수 (x, y 쌍이므로 /2)
        val numPoints = v1.size / 2

        for (i in 0 until numPoints) {
            val xIndex = i * 2
            val yIndex = i * 2 + 1

            val diffX = v1[xIndex] - v2[xIndex]
            val diffY = v1[yIndex] - v2[yIndex]
            val distanceSquared = (diffX * diffX) + (diffY * diffY)

            // ✨ 가중치 부여 (Weight)
            // 기본: 1.0
            // 팔꿈치(13,14) & 손목(15,16): 10.0 (민감도 10배!)
            // 어깨(11,12): 2.0
            val weight = when (i) {
                13, 14, 15, 16 -> 10.0 // 팔 동작이 다르면 다른 사진으로 취급
                11, 12 -> 2.0          // 어깨도 약간 중요
                0 -> 0.5               // 코 위치는 조금 변해도 관대하게 (얼굴 떨림 보정)
                else -> 1.0
            }

            sum += distanceSquared * weight
        }

        return sqrt(sum).toFloat()
    }

    private fun extractColorHistogram(bitmap: Bitmap): FloatArray {
        val scaled = bitmap.scale(32, 32, false)
        val pixels = IntArray(32 * 32)
        scaled.getPixels(pixels, 0, 32, 0, 0, 32, 32)

        val vector = FloatArray(32*32*3)

        for(i in pixels.indices) {
            val color = pixels[i]
            vector[i*3] = ((color shr 16) and 0xFF) / 255f
            vector[i*3+1] = ((color shr 8) and 0xFF) / 255f
            vector[i*3+2] = ((color) and 0xFF) / 255f
        }

        return vector

    }

    /**
     * ✨ [추가] 분석된 결과 리스트를 받아 중복 그룹을 찾아내는 함수
     * ViewModel에서 이 함수를 호출합니다.
     */
    suspend fun findDuplicateGroups(results: List<ImageAnalysisResult>): List<List<Uri>> {
        // CPU 연산이 많으므로 백그라운드 스레드 강제
        return withContext(Dispatchers.Default) {

            // 1. 시간순 정렬 (필수!)
            val sortedResults = results.sortedBy { it.dateTaken }

            val groups = mutableListOf<List<Uri>>()
            val visited = BooleanArray(sortedResults.size)

            // ✨ 비교 허용 시간 범위 (예: 10분 = 600,000ms)
            // 중복 사진이 10분 차이 이상 나면서 찍히는 경우는 거의 없음
            val TIME_THRESHOLD = 10 * 60 * 1000L

            for (i in sortedResults.indices) {
                if (visited[i]) continue

                val currentGroup = mutableListOf<Uri>()
                currentGroup.add(sortedResults[i].uri)
                visited[i] = true

                for (j in i + 1 until sortedResults.size) {
                    if (visited[j]) continue

                    // ✨ 핵심 최적화: 시간 차이가 임계값을 넘으면 더 이상 뒤쪽은 볼 필요 없음 (break)
                    val timeDiff = abs(sortedResults[i].dateTaken - sortedResults[j].dateTaken)
                    if (timeDiff > TIME_THRESHOLD) {
                        break // Inner Loop 탈출! (여기가 속도 향상의 열쇠)
                    }

                    // 시간 범위 안일 때만 무거운 ML 비교 수행
                    val similarity = calculateSimilarity(sortedResults[i], sortedResults[j])

                    if (similarity >= 0.994f) {
                        currentGroup.add(sortedResults[j].uri)
                        visited[j] = true
                    }
                }

                if (currentGroup.size > 1) {
                    groups.add(currentGroup)
                }
            }
            groups
        }
    }

}