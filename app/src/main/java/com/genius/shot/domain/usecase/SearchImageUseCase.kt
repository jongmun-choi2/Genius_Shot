package com.genius.shot.domain.usecase

import android.net.Uri
import com.genius.shot.data.db.ImageLabelDao
import com.genius.shot.domain.analyze.TranslationManager
import com.genius.shot.domain.model.GalleryItem
import com.genius.shot.domain.model.ImageItem
import javax.inject.Inject
import androidx.core.net.toUri

class SearchImageUseCase @Inject constructor(
    private val labelDao: ImageLabelDao, // ✨ DAO 사용
    private val translationManager: TranslationManager
) {
    // 반환 타입 변경: Flow<PagingData> -> List<GalleryItem.Image>
    // 이제 PagingSource가 필요 없습니다. DB에서 다 긁어오면 됩니다 (가벼움)
    suspend operator fun invoke(query: String): List<GalleryItem.Image> {
        if (query.isBlank()) return emptyList()

        // 1. 번역 (한글 -> 영어)
        val targetQuery = if (translationManager.containsKorean(query)) {
            translationManager.translateToEnglish(query)
        } else {
            query
        }

        // 2. DB 검색 (속도: 0.01초)
        val entities = labelDao.searchImages(targetQuery)

        // 3. UI 모델로 변환
        return entities.map { entity ->
            GalleryItem.Image(
                ImageItem(
                    id = entity.id,
                    uri = entity.uri.toUri(),
                    name = "",
                    dateTaken = entity.dateTaken
                )
            )
        }
    }

    // 번역 기능 노출 (ViewModel용)
    suspend fun translateQuery(query: String) =
        if(translationManager.containsKorean(query)) translationManager.translateToEnglish(query) else query
}