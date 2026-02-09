package com.genius.shot.domain.model

sealed interface GalleryItem {
    data class Image(val item: ImageItem) : GalleryItem
    data class DateHeader(val date: String) : GalleryItem
}