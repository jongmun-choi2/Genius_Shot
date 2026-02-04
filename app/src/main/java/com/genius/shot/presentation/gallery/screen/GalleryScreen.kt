package com.genius.shot.presentation.gallery.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.genius.shot.domain.model.GalleryItem
import com.genius.shot.presentation.gallery.component.DateHeader
import com.genius.shot.presentation.gallery.component.GalleryImageItem
import com.genius.shot.presentation.gallery.viewmodel.GalleryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBackClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {

    val pagingItems: LazyPagingItems<GalleryItem> = viewModel.galleryData.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("갤러리") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading) {
                Text(
                    text = "표시할 사진이 없습니다.\n사진을 찍거나 권한을 확인해주세요.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            val refreshState = pagingItems.loadState.refresh
            if(refreshState is LoadState.Error) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "불러오기 실패 \uD83D\uDE22", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text(text = refreshState.error.localizedMessage ?: "알 수 없는 오류")
                    Button(onClick = { pagingItems.retry() }) {
                        Text("다시 시도")
                    }
                }
            }

            if(refreshState is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if(pagingItems.itemCount > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { item ->
                            when(item) {
                                is GalleryItem.DateHeader -> "header_${item.date}"
                                is GalleryItem.Image -> "image_${item.item.id}"
                            }
                        },
                        contentType = pagingItems.itemContentType { item ->
                            when(item) {
                                is GalleryItem.DateHeader -> 0
                                is GalleryItem.Image -> 1
                            }
                        },
                        span = { index ->
                            when (pagingItems[index]) {
                                is GalleryItem.DateHeader -> GridItemSpan(3)
                                is GalleryItem.Image -> GridItemSpan(1)
                                null -> GridItemSpan(1)
                            }
                        }
                    ) { index ->
                        when( val item = pagingItems[index]) {
                            is GalleryItem.DateHeader -> DateHeader(date = item.date)
                            is GalleryItem.Image -> GalleryImageItem(image = item, onClick = {})
                            null -> {
                                Box(
                                    modifier = Modifier.aspectRatio(1f).background(Color.LightGray)
                                )
                            }
                        }
                    }
                }
            }

        }




    }

}