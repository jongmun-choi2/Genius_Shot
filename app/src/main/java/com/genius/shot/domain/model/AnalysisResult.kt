package com.genius.shot.domain.model

data class AnalysisResult(
    val results: List<ImageAnalysisResult>,
    val isLastBatch: Boolean,
    val analyzedCount: Int
)