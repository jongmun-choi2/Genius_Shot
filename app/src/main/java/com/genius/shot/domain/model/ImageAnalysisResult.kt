package com.genius.shot.domain.model

import android.net.Uri

data class ImageAnalysisResult(
    val uri: Uri,
    val hasPerson: Boolean,
    val vector: FloatArray,
    val dateTaken: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageAnalysisResult

        return vector.contentEquals(other.vector)
    }

    override fun hashCode() = vector.contentHashCode()

}
