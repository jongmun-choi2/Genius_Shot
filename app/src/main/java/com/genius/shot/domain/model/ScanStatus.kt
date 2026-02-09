package com.genius.shot.domain.model

import android.net.Uri

sealed interface ScanStatus {
    data class Progress(val message: String) : ScanStatus
    data class Complete(val duplicateGroups: List<ImageAnalysisResult>, val isLastPage: Boolean) : ScanStatus
    data class Error(val message: String) : ScanStatus
}