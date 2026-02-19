package com.genius.shot.domain.model

import android.net.Uri

data class ImageIdAndUri(
    val id: Long,
    val uri: Uri,
    val dateTaken: Long
)