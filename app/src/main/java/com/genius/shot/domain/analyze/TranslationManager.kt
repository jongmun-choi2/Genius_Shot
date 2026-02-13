package com.genius.shot.domain.analyze

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationManager @Inject constructor() {

    // 한글 -> 영어 번역기 설정
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.KOREAN)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val translator = Translation.getClient(options)

    // 모델 다운로드 조건 (와이파이 연결 시 등)
    private val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    /**
     * 한글을 영어로 번역합니다.
     * 모델이 없으면 다운로드를 시도합니다.
     */
    suspend fun translateToEnglish(text: String): String {
        return try {
            // 1. 모델이 준비되었는지 확인하고 없으면 다운로드
            translator.downloadModelIfNeeded(conditions).await()

            // 2. 번역 실행
            translator.translate(text).await()
        } catch (e: Exception) {
            e.printStackTrace()
            // 실패 시 원본 텍스트 반환 (또는 빈 문자열)
            text
        }
    }

    /**
     * 입력된 텍스트가 한글을 포함하는지 확인
     */
    fun containsKorean(text: String): Boolean {
        // 정규식으로 한글 범위 체크
        return text.matches(Regex(".*[가-힣]+.*"))
    }

    // 앱 종료 시 리소스 해제
    fun close() {
        translator.close()
    }
}