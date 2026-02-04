package com.genius.shot.presentation.gallery.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.genius.shot.domain.model.GalleryItem


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryImageItem(image: GalleryItem.Image, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        GlideImage(
            model = image.item.uri,
            contentDescription = image.item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}