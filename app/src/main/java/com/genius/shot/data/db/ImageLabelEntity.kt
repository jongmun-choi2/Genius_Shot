package com.genius.shot.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_labels")
data class ImageLabelEntity(
    @PrimaryKey val id: Long, // MediaStore ID와 동일
    val uri: String,
    val dateTaken: Long,
    val labels: String // "Cat,Dog,Sky" 형태의 문자열로 저장
)