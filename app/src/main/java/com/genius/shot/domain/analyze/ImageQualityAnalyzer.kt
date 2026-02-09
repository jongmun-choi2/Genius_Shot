package com.genius.shot.domain.analyze

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale

@Singleton
class ImageQualityAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver

            // 1. 이미지 크기만 먼저 읽기 (메모리 할당 X)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // InputStream을 열어서 크기 확인
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // 2. 샘플링 사이즈 계산 (목표: 640px 이하로 줄이기)
            // ML Kit는 480~640px 정도면 충분히 인식합니다.
            options.inSampleSize = calculateInSampleSize(options, 640, 640)
            options.inJustDecodeBounds = false // 이제 진짜로 읽기

            // 3. 리사이징하여 로드
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ✨ 구글 권장 리사이징 계산 로직
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // 목표 크기보다 클 때까지 계속 2배씩 줄임
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 사진이 흔들렸는지 판별하는 함수
     * @param bitmap: 분석할 이미지
     * @param threshold: 기준값 (기본 500. 이보다 낮으면 흔들림)
     * @return Boolean (true: 흔들림, false: 선명함)
     */
    fun isBlurry(uri: Uri, threshold: Int = 500): Boolean {

        val bitmap = loadBitmap(uri) ?: return false

        // 1. 성능을 위해 리사이징 (256x256 정도면 충분)
        // 원본(4000px)으로 하면 느리고, 너무 작으면 정확도가 떨어짐

        val scaledBitmap = bitmap.scale(256, 256, false)

        // 2. 라플라시안 변환 및 분산 계산
        val variance = calculateLaplacianVariance(scaledBitmap)

        // 3. 기준값과 비교
        return variance < threshold
    }

    /**
     * 라플라시안 분산(Laplacian Variance) 계산
     * - 이미지를 흑백으로 보고 엣지(Edge)의 선명도를 수치화함
     * - 값이 클수록 엣지가 선명함 (안 흔들림)
     * - 값이 작을수록 엣지가 뭉개짐 (흔들림)
     */
    private fun calculateLaplacianVariance(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 3x3 라플라시안 커널 (윤곽선 검출용 필터)
        //  0  1  0
        //  1 -4  1
        //  0  1  0
        val kernel = intArrayOf(0, 1, 0, 1, -4, 1, 0, 1, 0)

        var sum = 0.0
        var sqSum = 0.0
        var count = 0

        // 가장자리 1픽셀 제외하고 순회 (커널 연산 위해)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var newPixelVal = 0

                // 커널 적용 (Convolution)
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(y + ky) * width + (x + kx)]
                        // 흑백(Grayscale) 변환: (R+G+B)/3
                        // 더 정밀한 공식: 0.299*R + 0.587*G + 0.114*B 도 가능하지만 속도를 위해 평균 사용
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF
                        val gray = (r + g + b) / 3

                        newPixelVal += gray * kernel[(ky + 1) * 3 + (kx + 1)]
                    }
                }

                val value = newPixelVal.toDouble()
                sum += value
                sqSum += value * value
                count++
            }
        }

        // 분산(Variance) 공식: E[X^2] - (E[X])^2
        val mean = sum / count
        val variance = (sqSum / count) - (mean * mean)

        return variance
    }
}