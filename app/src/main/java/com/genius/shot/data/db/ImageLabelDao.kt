package com.genius.shot.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageLabelDao {
    // 검색 쿼리: 문자열 매칭 (빠름)
    @Query("SELECT * FROM image_labels WHERE labels LIKE '%' || :query || '%' ORDER BY dateTaken DESC")
    suspend fun searchImages(query: String): List<ImageLabelEntity>

    // 이미 분석된 ID 목록 가져오기 (중복 분석 방지)
    @Query("SELECT id FROM image_labels")
    suspend fun getAnalyzedImageIds(): List<Long>

    // 데이터 삽입 (KSP 에러 방지를 위해 Long 반환)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImageLabelEntity): Long
}