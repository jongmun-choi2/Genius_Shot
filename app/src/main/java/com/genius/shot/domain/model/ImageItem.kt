package com.genius.shot.domain.model

import android.net.Uri

data class ImageItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateTaken: Long
)